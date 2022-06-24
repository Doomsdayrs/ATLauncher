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
package com.atlauncher.gui.panels;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 24 / 06 / 2022
 * <p>
 * ViewModelPanel is a panel that implements {@link HierarchyPanel} to
 * destroy view models after a period of time. This is useful to ensure that
 * view models do not hog memory while idle.
 */
public abstract class ViewModelPanel<T extends Destroyable> extends HierarchyPanel {
    private static final Logger LOG = LogManager.getLogger(ViewModelPanel.class);

    /**
     * Time till view model should time out, in milliseconds.
     */
    private static final long DESTROY_TIMEOUT = 5000;

    /**
     * Supplier of the view model,
     * this is usually going to be a simple constructor call
     */
    private final Supplier<T> viewModelSupplier;

    /**
     * The last time the UI was hidden, used to calculate destroying.
     */
    private long lastHidden;

    public ViewModelPanel(LayoutManager layout, Supplier<T> supplier) {
        super(layout);
        viewModelSupplier = supplier;

        /*
         * Destroyer thread, will keep occurring to destroy the view model
         *  if the view is inactive.
         */
        Thread viewModelDestroyer = new Thread(() -> {
            while (true) {
                if (viewModel != null && !isShowing()) {
                    if (lastHidden + DESTROY_TIMEOUT < System.currentTimeMillis()) {
                        LOG.debug("Destroying view model");
                        try {
                            viewModel.destroy();
                        } catch (DestroyFailedException e) {
                            LOG.error("Failed to destroy view model", e);
                        } finally {
                            viewModel = null;
                            System.gc(); // Clean up
                        }
                    } else {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException ignored) {
                        }
                    }
                } else {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
        viewModelDestroyer.start();
    }

    @Nullable
    private T viewModel = null;

    /**
     * Get the view model. Might be dynamically created.
     * Do not expect this object to persist, do not save this object.
     *
     * @return The view model
     */
    @NotNull
    public T getViewModel() {
        if (viewModel == null) {
            viewModel = viewModelSupplier.get();
        }

        return viewModel;
    }

    /**
     * Overriding methods must call super.
     */
    @Override
    protected void onHide() {
        lastHidden = System.currentTimeMillis();
    }
}
