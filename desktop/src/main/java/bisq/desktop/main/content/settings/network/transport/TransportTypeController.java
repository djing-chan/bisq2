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

package bisq.desktop.main.content.settings.network.transport;

import bisq.desktop.ServiceProvider;
import bisq.desktop.common.view.Controller;
import bisq.network.common.TransportType;
import lombok.Getter;

public class TransportTypeController implements Controller {
    private final TransportTypeModel model;
    @Getter
    private final TransportTypeView view;

    public TransportTypeController(ServiceProvider serviceProvider, TransportType transportType) {
        model = new TransportTypeModel(serviceProvider, transportType);
        view = new TransportTypeView(model, this);
    }

    @Override
    public void onActivate() {
        model.updateLists();
    }

    @Override
    public void onDeactivate() {
        model.cleanup();
    }
}
