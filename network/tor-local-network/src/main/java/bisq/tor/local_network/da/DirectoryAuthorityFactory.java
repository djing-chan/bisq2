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

package bisq.tor.local_network.da;

import bisq.tor.local_network.da.keygen.process.DirectoryAuthorityKeyGenerator;
import bisq.tor.local_network.da.keygen.process.DirectoryIdentityKeyGenProcess;
import bisq.tor.local_network.da.keygen.RelayKeyGenProcess;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class DirectoryAuthorityFactory {

    @Getter
    private final Set<DirectoryAuthority> allDirectoryAuthorities = new HashSet<>();

    public void createDirectoryAuthority(DirectoryAuthority directoryAuthority,
                                         String passphrase) throws IOException, InterruptedException {
        Path dataDir = directoryAuthority.getDataDir();
        createDataDirIfNotPresent(dataDir);

        Path keysPath = dataDir.resolve("keys");
        boolean isSuccess = keysPath.toFile().mkdirs();
        if (!isSuccess) {
            throw new IllegalStateException("Couldn't create keys folder in data directory for directory authority.");
        }

        var relayKeyGenProcess = new RelayKeyGenProcess(directoryAuthority);
        String firstDirectoryAuthorityAddress = "127.0.0.1:" + directoryAuthority.getDirPort();
        var torDAKeyGenProcess = new DirectoryIdentityKeyGenProcess(keysPath, firstDirectoryAuthorityAddress);

        var directoryAuthorityKeyGenerator = new DirectoryAuthorityKeyGenerator(torDAKeyGenProcess, relayKeyGenProcess);
        directoryAuthorityKeyGenerator.generate(passphrase);

        directoryAuthority.setIdentityKeyFingerprint(
                directoryAuthorityKeyGenerator.getIdentityKeyFingerprint()
        );
        directoryAuthority.setRelayKeyFingerprint(
                directoryAuthorityKeyGenerator.getRelayKeyFingerprint()
        );

        allDirectoryAuthorities.add(directoryAuthority);
    }

    private void createDataDirIfNotPresent(Path dataDir) {
        File dataDirFile = dataDir.toFile();
        if (!dataDirFile.exists()) {
            boolean isSuccess = dataDir.toFile().mkdir();
            if (!isSuccess) {
                throw new IllegalStateException("Couldn't create data directory for directory authority.");
            }
        }
    }
}