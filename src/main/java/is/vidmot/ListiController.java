package is.vidmot;
/******************************************************************************
 *  Nafn    : Ebba Þóra Hvannberg
 *  T-póstur: ebba@hi.is
 *  Viðmótsforritun 2024
 *
 *  Controller fyrir lagalistann
 *  getur:
 *
 *  -- valið lag
 *  -- play / pause
 *  -- farið heim
 *****************************************************************************/
import is.vinnsla.Lag;
import is.vinnsla.Lagalistar;
import is.vinnsla.Lagalisti;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.Objects;


public class ListiController  {

    // fastar
    private final String PAUSE = "images/pause2.png";
    private final String PlAY = "images/play-button.png";
    private final String REPEATOFF = "images/repeatOff.png";
    private final String REPEATON = "images/repeatOn.png";

    // viðmótshlutir
    @FXML
    public ProgressBar fxProgressBar;   // progress bar fyrir spilun á lagi
    @FXML
    public ImageView repeatView; //Mynd fyrir repeat takka
    @FXML
    public ImageView fxShuffleBtn; // Mynd fyrir shuffle takka
    @FXML
    protected ImageView fxPlayPauseView; // mynd fyrir play/pause hnappinn
    @FXML
    protected ListView<Lag> fxListView; // lagalistinn
    @FXML
    private Button fxNotandi; // nafn notanda
    @FXML
    private Slider fxVolumeSlider;
    @FXML
    private MenuButton speedMenuButton; // Spilunarhraði

    // vinnslan
    private Lagalisti lagalisti; // lagalistinn
    private MediaPlayer player; // ein player breyta per forritið
    private Lag validLag;       // núverandi valið lag
    Boolean repeatFlag = false;

    /**
     * Frumstillir lagalistann og tengir hann við ListView viðmótshlut
     */

    public void initialize() {
        // setur lagalistann sem núverandi lagalista úr Lagalistar
        lagalisti = Lagalistar.getNuverandi();
        // tengdu lagalistann við ListView-ið
        fxListView.setItems(lagalisti.getListi());
        // man hvaða lag var síðast spilað á lagalistanum og setur það sem valið stak á ListView
        fxListView.getSelectionModel().select(lagalisti.getIndex());
        // setur lagið í focus
        fxListView.requestFocus();
        // // Lætur lagalista vita hvert valda lagið er í viðmótinu og uppfærir myndina fyrir lagið
        veljaLag();
        // setur upp player
        setjaPlayer();
        // setur nafn notenda og tekur burt takka ef engin er skráður inn
        if (Objects.equals(PlayerController.getNotandi(), "")){
            fxNotandi.setVisible(false);
        }else{
            fxNotandi.setText(PlayerController.getNotandi());
        }


        // Setur upphafsstöðu slider í 50%
        fxVolumeSlider.setValue(50.0);
        if (player != null) {
            player.setVolume(0.5); // Setur hljóðstyrk í 50%
        }

        fxVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (player != null) {
                player.setVolume(newValue.doubleValue() / 100.0); // Stilla hljóðstyrk á milli 0 og 1
            }
        });
        for (MenuItem item : speedMenuButton.getItems()) {
            item.setOnAction(event -> {
                String speedText = item.getText();
                double speed = Double.parseDouble(speedText.replace("x", ""));
                changePlaybackSpeed(speed);
            });
    }
    }

    /**
     * Bregðast við músaratburði og spila valið lag
     *
     * @param mouseEvent
     */

    @FXML
    protected void onValidLag(MouseEvent mouseEvent) {
        System.out.println(fxListView.getSelectionModel().getSelectedItem());
        // Lætur lagalista vita hvert valda lagið er í viðmótinu og uppfærir myndina fyrir lagið
        veljaLag();
        // spila lagið
        spilaLag();
    }

    /**
     * Lagið er pásað ef það er í spilun, lagið er spilað ef það er í pásu
     *
     * @param actionEvent ónotað
     */

    @FXML
    protected void onPlayPause(ActionEvent actionEvent) {
        // ef player-inn er spilandi
        if (player.getStatus().equals(MediaPlayer.Status.PLAYING)) {
            setjaMynd(fxPlayPauseView, PlAY);   // uppfærðu myndina með play (ör)
            player.pause();                     // pásaðu spilarann
        } else if (player.getStatus().equals(MediaPlayer.Status.PAUSED)) {
            setjaMynd(fxPlayPauseView, PAUSE);
            player.play();                      // haltu áfram að spila
        }
    }

    /**
     * Fara aftur í heima view. Ef spilari er til stöðva spilarann
     *
     * @param actionEvent ónotað
     */

    @FXML
    protected void onHeim(ActionEvent actionEvent) {
        // stoppaðu player ef hann er ekki null
        if (player != null)
            player.stop();
        // farðu í HEIMA senuna með ViewSwitcher
        ViewSwitcher.switchTo(View.HEIMA, true);
    }

    /**
     * Lætur laga lista vita hvert valda lagið er. Uppfærir myndina fyrir lagið.
     */
    private void veljaLag() {
        // hvaða lag er valið
        validLag = fxListView.getSelectionModel().getSelectedItem();
        //  láttu lagalista vita um indexinn á völdu lagi
        lagalisti.setIndex(fxListView.getSelectionModel().getSelectedIndex());
    }

    /**
     * Spila lagið
     */

    private void spilaLag() {
        /*setjaMynd(fxPlayPauseView, PAUSE);*/
        // Búa til nýjan player
        setjaPlayer();
        // setja spilun í gang
        player.play();
    }

    /**
     * Setja mynd með nafni á ImageView
     *
     * @param fxImageView viðmótshluturinn sem á að uppfærast
     * @param nafnMynd    nafn á myndinni
     */

    private void setjaMynd(ImageView fxImageView, String nafnMynd) {
        System.out.println ("nafn á mynd "+nafnMynd);
        fxImageView.setImage(new Image(getClass().getResource(nafnMynd).toExternalForm()));
    }

    /**
     * Setja upp player fyrir lagið, þ.m.t. at setja handler á hvenær lagið stoppar og tengja
     * lagið við progress bar
     */

    private void setjaPlayer() {
        // Stoppa player-inn ef hann var ekki stopp
        if (player != null)
            player.stop();
        // Smíða nýjan player með nýju Media fyrir lagið
        player = new MediaPlayer(new Media(getClass().getResource(validLag.getMedia()).toExternalForm()));
        // Láta player vita hvenær lagið endar - stop time
        player.setStopTime(new Duration(validLag.getLengd()));
        // setja fall sem er keyrð þegar lagið hættir
        player.setOnEndOfMedia(this::naestaLag);
        // setja listener tengingu á milli player og progress bar
        player.currentTimeProperty().addListener((observable, old, newValue) ->
                fxProgressBar.setProgress(newValue.divide(validLag.getLengd()).toMillis()));

    }

    /**
     * Næsta lag er spilað. Kallað á þessa aðferð þegar fyrra lag á listanum endar
     */
    private void naestaLag() {
        if (repeatFlag){
            // velja lag
            veljaLag();
            // spila lag
            spilaLag();
        }else{
            // setja valið lag sem næsta lag á núverandi lagalista
            lagalisti.naesti();
            // uppfæra ListView til samræmis, þ.e. að næsta lag sé valið
            fxListView.getSelectionModel().selectIndices(lagalisti.getIndex());
            // velja lag
            veljaLag();
            // spila lag
            spilaLag();
        }

    }

    public void onRepeat(ActionEvent actionEvent) {
        if (repeatFlag){
            setjaMynd(repeatView, REPEATOFF); // Breytur um mynd á takkanum
        }else {
            setjaMynd(repeatView, REPEATON); // Breytir um mynd á takkanum
        }

        repeatFlag = !repeatFlag; // Breytir um boolean gildi svo hægt sé að breyta á milli kveikt eða slökkt
    }


    public void onShuffle(ActionEvent actionEvent) {
    }

    /**
     * Stillir hraða spilunar út frá völdnu gildi
     * @param event
     */
    public void setPlaybackSpeed(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        String speed = menuItem.getText().replace("x", "");
        player.setRate(Double.parseDouble(speed));
    }

    /**
     * Stillir hraða
     * @param speed
     */
    private void changePlaybackSpeed(double speed) {
        if (player != null) {
            player.setRate(speed);
        }
    }
}


