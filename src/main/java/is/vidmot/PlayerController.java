package is.vidmot;
/******************************************************************************
 *  Nafn    : Ebba Þóra Hvannberg
 *  T-póstur: ebba@hi.is
 *  Viðmótsforritun 2024
 *
 *  Controller fyrir forsíðuna
 *
 *  Getur valið lagalista
 *  Spilað / pásað lög
 *  Repeat
 *  Shuffle
 *  Breytt spilunarhraða
 *  Búið til nýjan lagalista
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
import javafx.util.Pair;

import java.util.*;

public class PlayerController  {
    // Fastar
    public static ObservableList<String> selectedSongs = FXCollections.observableArrayList(); // Listi af lögum
    public static final String ASKRIFANDI = "Áskrifandi"; // Slóð á mynd
    private final String REPEATOFF = "images/repeatOff.png"; // Slóð á mynd
    private final String REPEATON = "images/repeatOn.png"; // Slóð á mynd
    private final String PlAY = "images/play2.png"; // Slóð á mynd
    private final String PAUSE = "images/pause2.png"; // Slóð á mynd
    private static String Notandi = ""; // Strengur fyrir innskráningu
    private static final String SHUFFLEON = "images/shuffleOn.png"; // Slóð á mynd
    private static final String SHUFFLEOFF = "images/shuffleOff.png"; // Slóð á mynd
    private double lastVolume = 50.0; // Breyta fyrir volume slider


    // viðmótshlutir
    @FXML
    public ImageView repeatView; // mynd fyrir repeat takkann
    @FXML
    public ListView fxSongView; // Sýna öll lögin
    @FXML
    public ProgressBar fxProgresssBar; // Progressbar fyrir lög
    @FXML
    public ImageView fxPlayPauseView; // Breyta fyrir imageview á play/pause takka
    @FXML
    public ImageView fxShuffleBtn; //Breyta fyrir imageview á shuffle takka
    @FXML
    protected Button fxAskrifandi; // Takki fyrir innskráningu
    @FXML
    private Slider fxVolumeSlider; // Slider til að hækka / lækka
    @FXML
    private MenuButton speedMenuButton; // Spilunarhraði
    Boolean repeatFlag = false; // Boolean gildi til að sjá hvort kveikt sé á repeat
    Boolean shuffleFlag = false; // Boolean gildi til að sjá hvort kveikt sé á shuffle
    @FXML
    private ListView<String> fxListView; // Breyta til að tengjast listview

    // vinnsla
    @FXML
    private MediaPlayer player; // ein player breyta per forritið
    private double currentPlaybackSpeed = 1.0; // breyta til að halda um playbackhraðan

    private List<String> originalSongList = new ArrayList<>(); // Listi af öllum lögunum


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
        // Determine the selected list index based on the selected item
        int selectedIndex = Integer.parseInt(selectedItem.substring(selectedItem.length() - 1)) - 1;

        // Switch to the selected list
        Lagalistar.setIndex(selectedIndex);

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
            if (shuffleFlag){
                int randomIndex = (int) (Math.random() * fxSongView.getItems().size());
                if (randomIndex == fxSongView.getSelectionModel().getSelectedIndex()){
                    randomIndex = randomIndex + 2;
                }
                fxSongView.getSelectionModel().select(randomIndex);
            } else {
                int currentIndex = fxSongView.getSelectionModel().getSelectedIndex();

                // Select the next song in the list (or loop back to the beginning if at the end)
                int nextIndex = (currentIndex + 1) % fxSongView.getItems().size();
                fxSongView.getSelectionModel().select(nextIndex);
            }
            String naesta = (String) fxSongView.getSelectionModel().getSelectedItem();
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

    /**
     * Þegar ýtt er á shuffle takka. Mynd á takkanum breytist og gildi shuffleFlag breytist og segir til
     * um hvort kveikt sé á takkanum
     * @param actionEvent
     */
    public void onShuffle(ActionEvent actionEvent) {

        if (shuffleFlag) {
            setjaMynd(fxShuffleBtn, SHUFFLEOFF);
        } else {
            setjaMynd(fxShuffleBtn, SHUFFLEON);
        }


        shuffleFlag = !shuffleFlag;

        if (shuffleFlag) {
            shuffleSongs();
        }
    }

    /**
     * Fall til að setja lagalista í handahófsröð / Shuffla listanum
     */
    private void shuffleSongs() {
        List<String> shuffledSongs = new ArrayList<>(originalSongList);
        Collections.shuffle(shuffledSongs);
    }

    /**
     * Fall sem keyrt þegar ýtt er á búa til lagalista. Setur upp dialog glugga
     * @param actionEvent
     */
    @FXML
    protected void onBuaTilLagalista(ActionEvent actionEvent) {
        Pair<String, List<String>> dialogResult = showSampleSelectionDialog();
        if (dialogResult != null && !dialogResult.getValue().isEmpty()) {
            String listName = dialogResult.getKey();
            List<String> selectedSamples = dialogResult.getValue();
            createNewListWithNamedSamples(listName, selectedSamples);

        }
    }

    /**
     * Býr til nýjan lagalista. Notandi velur nafn á listanum og hvaða lög hann vill hafa í honum.
     * @return
     */
    private Pair<String, List<String>> showSampleSelectionDialog() {
        Dialog<Pair<String, List<String>>> dialog = new Dialog<>();
        dialog.setTitle("Velja lög og nafn lista");
        dialog.setHeaderText("Halda inni shift til að velja mörg lög:");

        // Búa til ListView til að sýna samples
        ListView<String> listView = new ListView<>();
        listView.setItems(fxSongView.getItems());

        // Leyfa fjölda valkosta
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Músclick togglar selection
        listView.setOnMouseClicked(event -> {
            String selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (listView.getSelectionModel().getSelectedItems().contains(selectedItem)) {
                    listView.getSelectionModel().clearSelection(listView.getItems().indexOf(selectedItem));
                } else {
                    listView.getSelectionModel().select(selectedItem);
                }
            }
        });

        // Bú til textfield fyrir nafn listans
        TextField listNameTextField = new TextField();
        listNameTextField.setPromptText("Nafn lista");

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("Nafn lista:"), 0, 0);
        gridPane.add(listNameTextField, 1, 0);
        gridPane.add(listView, 1, 1);

        dialog.getDialogPane().setContent(gridPane);

        // button
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Umbreyta niðurstöðum frá dialog
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String listName = listNameTextField.getText();
                List<String> selectedSamples = new ArrayList<>(listView.getSelectionModel().getSelectedItems());
                return new Pair<>(listName, selectedSamples);
            }
            return null;
        });

        Optional<Pair<String, List<String>>> result = dialog.showAndWait();
        return result.orElse(null);
    }
    private void createNewListWithNamedSamples(String listName, List<String> samples) {
        List<String> existingSamplesInLagalisti = new ArrayList<>();

        for (String sample : samples) {
            if (fxSongView.getItems().contains(sample)) {
                existingSamplesInLagalisti.add(sample);
            }
        }

        System.out.println("Chosen songs in the new list '" + listName + "':");
        for (String chosenSong : existingSamplesInLagalisti) {
            System.out.println(chosenSong);
        }

        // You can add more logic here if needed
        ObservableList<String> newList = FXCollections.observableArrayList(existingSamplesInLagalisti);
        fxListView.getItems().add(listName);
        PlayerController.selectedSongs.addAll(existingSamplesInLagalisti);
    }}

