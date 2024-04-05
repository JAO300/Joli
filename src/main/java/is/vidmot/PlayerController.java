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
import is.vinnsla.Lagalistar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PlayerController  {

    // fastar
    public static final String ASKRIFANDI = "Áskrifandi";

    private final String REPEATOFF = "images/repeatOff.png";
    private final String REPEATON = "images/repeatOn.png";
    public ImageView repeatView; // mynd fyrir repeat takkann
    public ListView fxSongView; // Sýna öll lögin

    // viðmótshlutir
    @FXML
    protected Button fxAskrifandi;

    // Repeat takki
    Button repeatBtn;
    Boolean repeatFlag = false;

    @FXML
    private ListView<String> fxListView;


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
                if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
                    mp3Files.add(file.getName());
                }
            }
        }

        ObservableList<String> songList = FXCollections.observableArrayList(mp3Files);

        fxSongView.setItems(songList);
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
        utkoma.ifPresent (a -> {
            fxAskrifandi.setText(a.getNafn());});
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

}