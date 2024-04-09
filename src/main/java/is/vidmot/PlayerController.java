package is.vidmot;
/******************************************************************************
 *  Nafn    : Ebba Þóra Hvannberg
 *  T-póstur: ebba@hi.is
 *  Viðmótsforritun 2024
 *
 *  Controller fyrir forsíðuna
 *
 *  Getur valið lagalista
 *
 *****************************************************************************/
import is.vinnsla.Askrifandi;
import is.vinnsla.Lag;
import is.vinnsla.Lagalistar;
import is.vinnsla.Lagalisti;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseEvent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PlayerController  {

    // fastar
    public static final String ASKRIFANDI = "Áskrifandi";
    private final String REPEATOFF = "images/repeatOff.png";
    private final String REPEATON = "images/repeatOn.png";
    private final String PlAY = "images/play2.png";
    private final String PAUSE = "images/pause2.png";
    private static String Notandi = "";
    private static final String SHUFFLEON = "images/shuffleOn.png";
    private static final String SHUFFLEOFF = "images/shuffleOff.png";
    private double lastVolume = 50.0;


    // viðmótshlutir
    @FXML
    public ImageView repeatView; // mynd fyrir repeat takkann
    @FXML
    public ListView fxSongView; // Sýna öll lögin
    @FXML
    public ProgressBar fxProgresssBar;
    @FXML
    public ImageView fxPlayPauseView;
    @FXML
    public ImageView fxShuffleBtn;
    @FXML
    protected Button fxAskrifandi;
    @FXML
    private Slider fxVolumeSlider;
    @FXML
    private MenuButton speedMenuButton; // Spilunarhraði
    Boolean repeatFlag = false; // Boolean gildi til að sjá hvort kveikt sé á repeat
    Boolean shuffleFlag = false; // Boolean gildi til að sjá hvort kveikt sé á shuffle

    @FXML
    private ListView<String> fxListView;

    // vinnsla
    @FXML
    private MediaPlayer player; // ein player breyta per forritið

    // breyta til að halda um playbackhraðan
    private double currentPlaybackSpeed = 1.0;


    // frumstilling eftir að hlutur hefur verið smíðaður og .fxml skrá lesin
    public void initialize() {
        Lagalistar.frumstilla();
        ObservableList<String> items = FXCollections.observableArrayList(Lagalistar.getListarAsStringArray());
        fxListView.setItems(items);

        File mediaFolder = new File("src/main/resources/is/vidmot/media");

        List<String> mp3Files = new ArrayList<>();

        File[] files = mediaFolder.listFiles();

        if (files != null){
            for (File file : files) {
                if (file.isFile() && (file.getName().toLowerCase().endsWith(".mp3") || file.getName().toLowerCase().endsWith(".mp4"))) {
                    mp3Files.add(file.getName());
                }
            }
        }

        ObservableList<String> songList = FXCollections.observableArrayList(mp3Files);

        fxSongView.setItems(songList);
        fxSongView.setOnMouseClicked(this::onVeljaSample);


        // Setur upphafsstöðu slider í 50%
        fxVolumeSlider.setValue(50.0);


        fxVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (player != null) {
                player.setVolume(newValue.doubleValue() / 100.0); // Stilla hljóðstyrk á milli 0 og 1
            }
        });

        // Sækir playback hraða úr speedMenuButton og stillir hann fyrir lagaspilara.
        for (MenuItem item : speedMenuButton.getItems()) {
            item.setOnAction(event -> {
                String speedText = item.getText();
                double speed = Double.parseDouble(speedText.replace("x", ""));
                changePlaybackSpeed(speed);
            });
        }
    }


    /**
     * Atburðarhandler fyrir að velja lagalista. Sá lagalisti er settur og farið í senu fyrir þann lista
     * @param mouseEvent
     */
    @FXML
    protected void onVeljaLista(MouseEvent mouseEvent) {
        //Hvaða listi var valinn
        String selectedItem = fxListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null)
            System.out.println("Valinn listi: " + selectedItem);
        // skiptum yfir í LAGALISTI view
        ViewSwitcher.switchTo(View.LAGALISTI, false);
        //Stoppa player ef lag er í gangi
        player.stop();
    }
    /**
     * Loggar áskrifanda inn
     *
     * @param actionEvent
     */
    public void onLogin(ActionEvent actionEvent) {
        // býr til nýjan dialog með tómum áskrifanda
        AskrifandiDialog dialog = new AskrifandiDialog(new Askrifandi(ASKRIFANDI));
        // sýndu dialoginn
        Optional<Askrifandi> utkoma = dialog.showAndWait();
        // Ef fékkst svar úr dialognum setjum við nafnið á áskrifandanum í notendaviðmótið
        utkoma.ifPresent(a -> {
            fxAskrifandi.setText(a.getNafn());
            //geymdir nafn notenda
            Notandi = a.getNafn();
        });
    }


    public static String getNotandi() {
        return Notandi;
    }

    /**
     * Atburðahandler fyrir repeat takkan. Breytir um mynd á takkanum og spilar sama lagið aftur og aftur.
     * @param actionEvent
     */
    public void onRepeat(ActionEvent actionEvent) {

        if (repeatFlag){
            setjaMynd(repeatView, REPEATOFF); // Breytur um mynd á takkanum
        }else {
            setjaMynd(repeatView, REPEATON); // Breytir um mynd á takkanum
        }

        repeatFlag = !repeatFlag; // Breytir um boolean gildi svo hægt sé að breyta á milli kveikt eða slökkt
        player.setRate(currentPlaybackSpeed); //playback hraði
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
     * Bregðas við músaatriði og spila sample
     * @param mouseEvent
     */
    @FXML
    protected void onVeljaSample(MouseEvent mouseEvent) {
        String validLag = (String) fxSongView.getSelectionModel().getSelectedItem();

        if (validLag != null) {
            String mediaPath = "src/main/resources/is/vidmot/media/" + validLag;

            Media media = new Media(new File(mediaPath).toURI().toString());

            if (player != null) {
                player.stop();
            }

            player = new MediaPlayer(media);

            // Setja volume gildi inn
            player.setVolume(lastVolume / 100.0);

            // uppfæra lastVolume breytu þegar hún breytist
            player.volumeProperty().addListener((observable, oldValue, newValue) -> {
                lastVolume = newValue.doubleValue() * 100.0;
            });

            // Halda volume gildinu í því sama og það var síðast í
            fxVolumeSlider.setValue(lastVolume);

            player.play();

            player.setOnEndOfMedia(this::naestaLag);

            player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                Duration duration = player.getMedia().getDuration();
                double progress = newValue.toSeconds() / duration.toSeconds();
                fxProgresssBar.setProgress(progress);
            });
        }
        player.setRate(currentPlaybackSpeed); //playback hraði
    }

    /**
     * Spilar / pásar lagið og breytir um mynd á takkanum samkvæmt því
     * @param actionEvent
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
     * Spilar næsta lag
     */
    private void naestaLag(){
        // Get the index of the currently playing song
        if (repeatFlag){
            String validLag = (String) fxSongView.getSelectionModel().getSelectedItem();
            spilaLag(validLag);
        }else {
            int currentIndex = fxSongView.getSelectionModel().getSelectedIndex();

            // Select the next song in the list (or loop back to the beginning if at the end)
            int nextIndex = (currentIndex + 1) % fxSongView.getItems().size();
            fxSongView.getSelectionModel().select(nextIndex);
            String naesta = (String) fxSongView.getItems().get(nextIndex);
            spilaLag(naesta);
        }
        player.setRate(currentPlaybackSpeed); //playback hraði

    }

    /**
     * Spilar lagið sem er valið
     * @param nafn nafnið á laginu
     */
    private void spilaLag(String nafn){
        String mediaPath = "src/main/resources/is/vidmot/media/" + nafn;

        Media media = new Media(new File(mediaPath).toURI().toString());
        player.stop();
        player = new MediaPlayer(media);
        player.play();

        player.setOnEndOfMedia(this::naestaLag);

        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            Duration duration = player.getMedia().getDuration();
            double progress = newValue.toSeconds() / duration.toSeconds();
            fxProgresssBar.setProgress(progress);
        });

    }


    /**
>>>>>>> main
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
            currentPlaybackSpeed = speed; //uppfærir breytuna
        }
    }



    public void onShuffle(ActionEvent actionEvent) {
    if (shuffleFlag){
        setjaMynd(fxShuffleBtn, SHUFFLEOFF);
    }else {
        setjaMynd(fxShuffleBtn, SHUFFLEON);
    }

    shuffleFlag = !shuffleFlag;
}
}

