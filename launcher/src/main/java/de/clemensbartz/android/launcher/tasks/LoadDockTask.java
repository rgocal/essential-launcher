/*
 * Copyright (C) 2018  Clemens Bartz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.clemensbartz.android.launcher.tasks;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.StringTokenizer;

import de.clemensbartz.android.launcher.controllers.DockController;
import de.clemensbartz.android.launcher.daos.SharedPreferencesDAO;
import de.clemensbartz.android.launcher.models.ApplicationModel;

/**
 * This task will load the items in the dock.
 * @author Clemens Bartz
 * @since 2.0
 */
public final class LoadDockTask extends AsyncTask<Integer, LoadDockTask.LoadDockTaskProgress, Integer> {

    /** Weak reference for the shared preference dao. */
    private final WeakReference<SharedPreferencesDAO> sharedPreferencesDAOWeakReference;
    /** Weak reference for the dock controller. */
    private final WeakReference<DockController> dockControllerWeakReference;

    /**
     * Create a new task to load all dock items.
     * @param sharedPreferencesDAO the shared preference dao
     * @param dockController the dock controller
     */
    public LoadDockTask(final SharedPreferencesDAO sharedPreferencesDAO, final DockController dockController) {
        sharedPreferencesDAOWeakReference = new WeakReference<>(sharedPreferencesDAO);
        dockControllerWeakReference = new WeakReference<>(dockController);
    }

    @Override
    protected void onPreExecute() {
        final DockController dockController = dockControllerWeakReference.get();

        if (dockController != null) {
            for (int i = 0; i < DockController.NUMBER_OF_ITEMS; i++) {
                dockController.clearIndex(i);
            }
        }
    }

    @Override
    protected Integer doInBackground(final Integer... integers) {

        final SharedPreferencesDAO sharedPreferencesDAO = sharedPreferencesDAOWeakReference.get();

        if (sharedPreferencesDAO != null) {
            for (int i = 0; i < DockController.NUMBER_OF_ITEMS; i++) {
                if (isCancelled()) {
                    break;
                }

                final String key = DockController.PIN_PREFIX + Integer.toString(i);

                final String value = sharedPreferencesDAO.getString(key, "");

                if (value.length() <= 0) {
                    continue;
                }

                final StringTokenizer tokenizer = new StringTokenizer(value, DockController.SEPARATOR);

                if (tokenizer.countTokens() != 2) {
                    continue;
                }

                final ApplicationModel applicationModel = new ApplicationModel();
                applicationModel.packageName = tokenizer.nextToken();
                applicationModel.className = tokenizer.nextToken();

                final LoadDockTaskProgress progress = new LoadDockTaskProgress();
                progress.index = i;
                progress.applicationModel = applicationModel;

                publishProgress(progress);
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(final LoadDockTaskProgress... values) {
        final DockController dockController = dockControllerWeakReference.get();

        if (dockController != null) {
            for (final LoadDockTaskProgress progress : values) {
                dockController.updateDock(progress.index, progress.applicationModel);
            }
        }
    }

    /**
     * Inner class to publish progress.
     * @author Clemens Bartz
     * @since 2.0
     */
    static final class LoadDockTaskProgress {
        /** Name of the component to update. */
        ApplicationModel applicationModel;
        /** Index to update. */
        int index;
    }
}