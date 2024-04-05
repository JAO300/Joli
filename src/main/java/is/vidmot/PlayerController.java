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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.Optional;
import java.util.Set;

public class PlayerController  {

    // fastar
    public static final String ASKRIFANDI = "Áskrifandi";

    private final String REPEATOFF = "images/repeatOff.png";
    private final String REPEATON = "images/repeatOn.png";
    public ImageView repeatView; // mynd fyrir repeat takkann

    // viðmótshlutir
    @FXML
    protected Button fxAskrifandi;

    // Repeat takki
    Button repeatBtn;
    Boolean repeatFlag = false;

    // frumstilling eftir að hlutur hefur verið smíðaður og .fxml skrá lesin
    public void initialize() {
        Lagalistar.frumstilla();
    }

    /**
     * Atburðarhandler fyrir að velja lagalista. Sá lagalisti er settur og farið í senu fyrir þann lista
     * @param mouseEvent
     */
    @FXML
    protected void onVeljaLista(ActionEvent mouseEvent) {
        // hvaða reitur var valinn
        int i = GridPane.getRowIndex((Node) mouseEvent.getSource());
        int j = GridPane.getColumnIndex((Node) mouseEvent.getSource());
        // skiptum yfir í lagalistann í vinnslunni sem var valið
        Lagalistar.setIndex(i * 2 + j);
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