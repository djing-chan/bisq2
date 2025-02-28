/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.main.content.bisq_easy.trade_wizard.price;

import bisq.desktop.common.utils.ImageUtil;
import bisq.desktop.common.view.View;
import bisq.desktop.components.controls.BisqTooltip;
import bisq.desktop.components.controls.MaterialTextField;
import bisq.desktop.main.content.bisq_easy.components.PriceInput;
import bisq.i18n.Res;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

@Slf4j
public class TradeWizardPriceView extends View<VBox, TradeWizardPriceModel, TradeWizardPriceController> {
    private final MaterialTextField percentage;
    private final ToggleButton useFixPriceToggle;
    private final VBox fieldsBox;
    private final PriceInput priceInput;
    private Subscription percentageFocussedPin, useFixPricePin;

    public TradeWizardPriceView(TradeWizardPriceModel model, TradeWizardPriceController controller, PriceInput priceInput) {
        super(new VBox(10), model, controller);
        this.priceInput = priceInput;

        root.setAlignment(Pos.TOP_CENTER);

        Label headline = new Label(Res.get("bisqEasy.price.headline"));
        headline.getStyleClass().add("bisq-text-headline-2");

        Label subtitleLabel = new Label(Res.get("bisqEasy.createOffer.price.subtitle"));
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.getStyleClass().add("bisq-text-3");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(500);

        percentage = new MaterialTextField(Res.get("bisqEasy.price.percentage"));

        useFixPriceToggle = new ToggleButton();
        useFixPriceToggle.setGraphic(ImageUtil.getImageViewById("arrows-up-down"));
        useFixPriceToggle.getStyleClass().add("icon-button");
        useFixPriceToggle.setTooltip(new BisqTooltip(Res.get("bisqEasy.price.toggle.tooltip")));

        fieldsBox = new VBox(20);
        fieldsBox.setAlignment(Pos.TOP_CENTER);
        fieldsBox.setMinWidth(400);

        HBox.setMargin(fieldsBox, new Insets(0, 0, 0, 44));
        HBox hBox = new HBox(10, fieldsBox, useFixPriceToggle);
        hBox.setAlignment(Pos.CENTER);
        VBox.setMargin(headline, new Insets(60, 0, 0, 0));
        root.getChildren().addAll(headline, subtitleLabel, hBox);
    }

    @Override
    protected void onViewAttached() {
        percentage.textProperty().bindBidirectional(model.getPercentageAsString());

        percentageFocussedPin = EasyBind.subscribe(percentage.textInputFocusedProperty(), controller::onPercentageFocussed);
        useFixPriceToggle.setSelected(model.getUseFixPrice().get());
        useFixPriceToggle.setOnAction(e -> controller.onToggleUseFixPrice());
        useFixPricePin = EasyBind.subscribe(model.getUseFixPrice(), useFixPrice -> {
            fieldsBox.getChildren().clear();
            Node firstChild = useFixPrice ? priceInput.getRoot() : percentage;
            Node lastChild = useFixPrice ? percentage : priceInput.getRoot();
            fieldsBox.getChildren().addAll(firstChild, lastChild);
            if (useFixPrice) {
                percentage.setEditable(false);
                percentage.deselect();
                priceInput.setEditable(true);
                priceInput.requestFocus();
            } else {
                priceInput.setEditable(false);
                priceInput.deselect();
                percentage.setEditable(true);
                percentage.requestFocus();
            }
        });

        // Needed to trigger focusOut event on amount components
        // We handle all parents mouse events.
        Parent node = root;
        while (node.getParent() != null) {
            node.setOnMousePressed(e -> root.requestFocus());
            node = node.getParent();
        }
    }

    @Override
    protected void onViewDetached() {
        percentage.textProperty().unbindBidirectional(model.getPercentageAsString());

        percentageFocussedPin.unsubscribe();
        useFixPricePin.unsubscribe();

        useFixPriceToggle.setOnAction(null);

        Parent node = root;
        while (node.getParent() != null) {
            node.setOnMousePressed(null);
            node = node.getParent();
        }
    }
}
