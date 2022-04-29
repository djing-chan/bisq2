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

package bisq.desktop.primary.main.content.social.components;

import bisq.common.data.ByteArray;
import bisq.desktop.components.robohash.RoboHash;
import bisq.desktop.layout.Layout;
import bisq.i18n.Res;
import bisq.social.user.ChatUser;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import java.util.stream.Collectors;

@Slf4j
public class ChatUserOverview implements Comparable<ChatUserOverview> {
    private final Controller controller;

    public ChatUserOverview(ChatUser chatUser) {
        this(chatUser, false);
    }

    public ChatUserOverview(ChatUser chatUser, boolean ignored) {
        controller = new Controller(chatUser);
        controller.model.ignored = ignored;
    }

    public Pane getRoot() {
        return controller.view.getRoot();
    }

    public boolean isIgnored() {
        return controller.model.ignored;
    }

    public ChatUser getChatUser() {
        return controller.model.chatUser;
    }

    @Override
    public int compareTo(ChatUserOverview o) {
        return controller.model.chatUser.getNym().compareTo(o.controller.model.chatUser.getNym());
    }

    private static class Controller implements bisq.desktop.common.view.Controller {
        private final Model model;
        @Getter
        private final View view;

        private Controller(ChatUser chatUser) {
            model = new Model(chatUser);
            view = new View(model, this);
        }

        @Override
        public void onActivate() {
            ChatUser chatUser = model.chatUser;
            if (chatUser == null) {
                return;
            }

            model.id.set(chatUser.getId());
            model.userName.set(chatUser.getUserName());
            model.roboHashNode.set(RoboHash.getImage(new ByteArray(chatUser.getPubKeyHash())));
            String entitledRoles = chatUser.getRoles().stream().map(e -> Res.get(e.type().name())).collect(Collectors.joining(", "));
            model.entitlements.set(Res.get("social.createUserProfile.entitledRoles", entitledRoles));
            model.entitlementsVisible.set(!chatUser.getRoles().isEmpty());
        }

        @Override
        public void onDeactivate() {
        }
    }

    private static class Model implements bisq.desktop.common.view.Model {
        private final ChatUser chatUser;
        private boolean ignored;
        private final ObjectProperty<Image> roboHashNode = new SimpleObjectProperty<>();
        private final StringProperty userName = new SimpleStringProperty();
        private final StringProperty id = new SimpleStringProperty();
        private final BooleanProperty entitlementsVisible = new SimpleBooleanProperty();
        private final StringProperty entitlements = new SimpleStringProperty();

        private Model(ChatUser chatUser) {
            this.chatUser = chatUser;
        }
    }

    @Slf4j
    public static class View extends bisq.desktop.common.view.View<HBox, Model, Controller> {
        private final ImageView roboIcon;
        private final Label userName, id, entitlements;
        private Subscription roboHashNodeSubscription;

        private View(Model model, Controller controller) {
            super(new HBox(), model, controller);

            root.setSpacing(10);
            root.setAlignment(Pos.CENTER_LEFT);

            userName = new Label();
            userName.setMaxWidth(100);
            Tooltip.install(userName, new Tooltip(model.chatUser.getTooltipString()));

            roboIcon = new ImageView();
            roboIcon.setFitWidth(37.5);
            roboIcon.setFitHeight(37.5);
            Tooltip.install(roboIcon, new Tooltip(model.chatUser.getTooltipString()));

            HBox hBox = Layout.hBoxWith(roboIcon, userName);
            hBox.setAlignment(Pos.CENTER_LEFT);

            ImageView trust = new ImageView();
            trust.setId("trust");
            trust.setX(20);
            trust.setY(20);

            StackPane icons = new StackPane();
            icons.getChildren().addAll(roboIcon, trust);

            root.getChildren().addAll(icons, userName);

            // todo add tooltip overlay for id and entitlements
            id = new Label();
            id.getStyleClass().add("offer-label-small");
            id.setPadding(new Insets(-5, 0, 0, 0));

            entitlements = new Label();
            entitlements.getStyleClass().add("offer-label-small");
            entitlements.setPadding(new Insets(-5, 0, 0, 0));
        }

        @Override
        protected void onViewAttached() {
            userName.textProperty().bind(model.userName);
            id.textProperty().bind(model.id);
            entitlements.textProperty().bind(model.entitlements);
            entitlements.visibleProperty().bind(model.entitlementsVisible);
            entitlements.managedProperty().bind(model.entitlementsVisible);

            roboHashNodeSubscription = EasyBind.subscribe(model.roboHashNode, roboIcon -> {
                if (roboIcon != null) {
                    this.roboIcon.setImage(roboIcon);
                }
            });
        }

        @Override
        protected void onViewDetached() {
            userName.textProperty().unbind();
            id.textProperty().unbind();
            entitlements.textProperty().unbind();
            entitlements.visibleProperty().unbind();
            entitlements.managedProperty().unbind();
            roboHashNodeSubscription.unsubscribe();
        }
    }
}