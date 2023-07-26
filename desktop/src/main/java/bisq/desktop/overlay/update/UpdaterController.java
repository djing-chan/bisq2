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

package bisq.desktop.overlay.update;

import bisq.common.observable.Pin;
import bisq.desktop.ServiceProvider;
import bisq.desktop.common.Browser;
import bisq.desktop.common.observable.FxBindings;
import bisq.desktop.common.threading.UIThread;
import bisq.desktop.common.view.Controller;
import bisq.desktop.components.overlay.Popup;
import bisq.desktop.overlay.OverlayController;
import bisq.settings.CookieKey;
import bisq.settings.SettingsService;
import bisq.update.DownloadDescriptor;
import bisq.update.UpdateService;
import javafx.application.Platform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CancellationException;

import static bisq.update.UpdateService.RELEASES_URL;

@Slf4j
public class UpdaterController implements Controller {
    private final UpdaterModel model;
    @Getter
    private final UpdaterView view;
    private final ServiceProvider serviceProvider;
    private final SettingsService settingsService;
    private final UpdateService updateService;
    private Pin getDownloadInfoListPin, releaseNotificationPin;

    public UpdaterController(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        settingsService = serviceProvider.getSettingsService();
        updateService = serviceProvider.getUpdateService();
        model = new UpdaterModel();
        view = new UpdaterView(model, this);
    }

    @Override
    public void onActivate() {
        getDownloadInfoListPin = FxBindings.<DownloadDescriptor, UpdaterView.ListItem>bind(model.getListItems())
                .map(UpdaterView.ListItem::new)
                .to(updateService.getDownloadDescriptorList());

        releaseNotificationPin = updateService.getReleaseNotification().addObserver(releaseNotification -> {
            if (releaseNotification == null) {
                return;
            }
            String version = releaseNotification.getVersionString();
            model.getVersion().set(version);
            model.getReleaseNotes().set(releaseNotification.getReleaseNotes());
            model.getDownloadUrl().set(RELEASES_URL + version);
        });
    }

    @Override
    public void onDeactivate() {
        getDownloadInfoListPin.unbind();
        releaseNotificationPin.unbind();
    }

    void onDownload() {
        model.getTableVisible().set(true);
        try {
            updateService.downloadAndVerify()
                    .whenComplete((__, throwable) -> {
                        if (throwable == null) {
                            model.getDownloadAndVerifyCompleted().set(true);
                        } else if (!(throwable instanceof CancellationException)) {
                            UIThread.run(() -> new Popup().error(throwable).show());
                        }
                    });
        } catch (IOException e) {
            UIThread.run(() -> new Popup().error(e).show());
        }
    }

    void onDownloadLater() {
        OverlayController.hide();
    }

    void onIgnore() {
        settingsService.setCookie(CookieKey.IGNORE_VERSION, model.getVersion().get(), true);

        OverlayController.hide();
    }

    void onRestart() {
        serviceProvider.getShotDownHandler().shutdown().thenAccept(result -> Platform.exit());
    }

    void onOpenUrl() {
        Browser.open(model.getDownloadUrl().get());
    }
}