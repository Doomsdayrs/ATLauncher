/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.gui.tabs.settings;

import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 */
public interface INetworkSettingsViewModel extends IAbstractSettingsViewModel {

    int getConcurrentConnections();

    void setConcurrentConnections(int connections);

    void addOnConcurrentConnectionsChanged(Consumer<Integer> onChanged);

    int getConnectionTimeout();

    void setConnectionTimeout(int timeout);

    void addOnConnectionTimeoutChanged(Consumer<Integer> onChanged);

    void setDoNotUseHTTP2(Boolean b);

    void addOnDoNotUseHTTP2Changed(Consumer<Boolean> onChanged);

    void setEnableProxy(Boolean b);

    void addOnEnableProxyChanged(Consumer<Boolean> onChanged);

    void setProxyHost(String host);

    void addOnProxyHostChanged(Consumer<String> onChanged);

    int getProxyPort();

    void setProxyPort(int port);

    void addOnProxyPortChanged(Consumer<Integer> onChanged);

    enum ProxyType {
        HTTP, SOCKS, DIRECT
    }

    void setProxyType(ProxyType type);

    void addOnProxyTypeChanged(Consumer<Integer> onChanged);

    void addOnProxyCheckListener(Consumer<ProxyCheckState> onChecked);

    abstract class ProxyCheckState {

        static class NotChecking extends ProxyCheckState {
        }

        static class CheckPending extends ProxyCheckState {
        }

        static class Checking extends ProxyCheckState {
        }

        static class Checked extends ProxyCheckState {
            final boolean valid;

            Checked(boolean valid) {
                this.valid = valid;
            }
        }
    }
}