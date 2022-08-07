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

package bisq.desktop.primary.main.content.trade;

import bisq.desktop.common.Browser;
import bisq.desktop.common.utils.ImageUtil;
import bisq.desktop.common.view.Controller;
import bisq.desktop.common.view.Model;
import bisq.desktop.common.view.View;
import bisq.i18n.Res;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

@Slf4j
public abstract class ProtocolRoadmapView<M extends Model, C extends Controller> extends View<VBox, M, C> {
    protected final Label headline, subHeadline, overviewHeadline, overview, releaseHeadline, release, tradeOffsHeadline, tradeOffs;
    protected final Hyperlink learnMore;
    private Subscription heightPin;

    public ProtocolRoadmapView(M model, C controller) {
        super(new VBox(10), model, controller);

        String key = getKey();
        headline = new Label(Res.get("trade.protocols." + key));
        headline.setGraphic(ImageUtil.getImageViewById(getIconId()));
        headline.getStyleClass().addAll("font-size-20", "font-light");
        headline.setGraphicTextGap(10);
        headline.setWrapText(true);

        subHeadline = new Label(Res.get("trade.protocols." + key + ".subHeadline"));
        subHeadline.getStyleClass().addAll("font-size-14", "font-light", "text-fill-grey-dimmed");
        subHeadline.setWrapText(true);

        overviewHeadline = new Label(Res.get("trade.protocols.overview"));
        overviewHeadline.getStyleClass().addAll("font-size-16", "font-light");
        overviewHeadline.setWrapText(true);

        overview = new Label(Res.get("trade.protocols." + key + ".overview"));
        overview.getStyleClass().addAll("font-size-12", "font-light", "bisq-line-spacing-02");
        overview.setWrapText(true);

        releaseHeadline = new Label(Res.get("trade.protocols.release"));
        releaseHeadline.getStyleClass().addAll("font-size-16", "font-light");
        releaseHeadline.setWrapText(true);

        release = new Label(Res.get("trade.protocols." + key + ".release"));
        release.getStyleClass().addAll("font-size-12", "font-light");
        release.setWrapText(true);

        tradeOffsHeadline = new Label(Res.get("trade.protocols.tradeOffs"));
        tradeOffsHeadline.getStyleClass().addAll("font-size-16", "font-light");
        tradeOffsHeadline.setWrapText(true);

        tradeOffs = new Label(Res.get("trade.protocols." + key + ".tradeOffs"));
        tradeOffs.getStyleClass().addAll("font-size-12", "font-light", "bisq-line-spacing-02");
        tradeOffs.setWrapText(true);

        learnMore = new Hyperlink(Res.get("learnMore"));
        learnMore.getStyleClass().addAll("font-size-12", "text-fill-green");

        VBox.setMargin(headline, new Insets(0, 0, 0, 0));
        VBox.setMargin(overviewHeadline, new Insets(25, 0, 0, 0));
        VBox.setMargin(releaseHeadline, new Insets(35, 0, 0, 0));
        VBox.setMargin(tradeOffsHeadline, new Insets(35, 0, 0, 0));
        VBox.setMargin(tradeOffs, new Insets(0, 0, 15, 0));
        root.getChildren().addAll(headline, subHeadline,
                overviewHeadline, overview,
                releaseHeadline, release,
                tradeOffsHeadline, tradeOffs,
                learnMore);

    }

    abstract protected String getIconId();

    abstract protected String getKey();

    abstract protected String getUrl();

    @Override
    protected void onViewAttached() {
        learnMore.setOnAction(e -> Browser.open(getUrl()));

        root.setMinHeight(2500);
        heightPin = EasyBind.subscribe(tradeOffs.heightProperty(), h -> {
            if (tradeOffs.getHeight() > 0) {

                headline.setMinHeight(Math.max(20, headline.getHeight()));
                subHeadline.setMinHeight(Math.max(20, subHeadline.getHeight()));

                overviewHeadline.setMinHeight(Math.max(20, overviewHeadline.getHeight()));
                overview.setMinHeight(Math.max(20, overview.getHeight()));

                releaseHeadline.setMinHeight(Math.max(20, releaseHeadline.getHeight()));
                release.setMinHeight(Math.max(20, release.getHeight()));

                tradeOffsHeadline.setMinHeight(Math.max(20, tradeOffsHeadline.getHeight()));
                tradeOffs.setMinHeight(Math.max(20, tradeOffs.getHeight()));

                root.setMinHeight(Region.USE_COMPUTED_SIZE);
                heightPin.unsubscribe();
            }
        });
    }

    @Override
    protected void onViewDetached() {
        learnMore.setOnAction(null);
        heightPin.unsubscribe();
    }
}