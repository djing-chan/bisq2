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

package bisq.desktop.main.content.user.bonded_roles.tabs.registration;

import bisq.bonded_roles.BondedRoleType;
import bisq.desktop.common.view.Model;
import bisq.network.common.AddressByTransportTypeMap;
import bisq.user.identity.UserIdentity;
import javafx.beans.property.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class BondedRolesRegistrationModel implements Model {
    protected final BondedRoleType bondedRoleType;
    protected final ObjectProperty<UserIdentity> selectedChatUserIdentity = new SimpleObjectProperty<>();
    protected final StringProperty profileId = new SimpleStringProperty();
    protected final StringProperty bondUserName = new SimpleStringProperty();
    protected final StringProperty signature = new SimpleStringProperty();
    protected final BooleanProperty requestButtonDisabled = new SimpleBooleanProperty();
    protected final BooleanProperty requestCancellationButtonVisible = new SimpleBooleanProperty();
    private final BooleanProperty isCollapsed = new SimpleBooleanProperty();
    protected final AddressByTransportTypeMap addressByNetworkType = new AddressByTransportTypeMap();
    @Setter
    protected String authorizedPublicKey;

    public BondedRolesRegistrationModel(BondedRoleType bondedRoleType) {
        this.bondedRoleType = bondedRoleType;
    }
}
