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
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class PlayerController  {

    // fastar
    public static final String ASKRIFANDI = "Áskrifandi";

    // viðmótshlutir
    @FXML
    protected Button fxAskrifandi;

    @FXML
    private ListView<String> fxListView;

    // frumstilling eftir að hlutur hefur verið smíðaður og .fxml skrá lesin
    public void initialize() {
        Lagalistar.frumstilla();
        ObservableList<String> items = FXCollections.observableArrayList(Lagalistar.getListarAsStringArray());
        fxListView.setItems(items);
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
}