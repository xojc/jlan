/*
 * Copyright (C) 2006-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.jlan.server.filesys.cache.hazelcast;

import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.cluster.ClusterFileState;

import com.hazelcast.core.IMap;

/**
 * Update File State Task Class
 *
 * <p>
 * Update a file state using a synchronous update.
 *
 * @author gkspencer
 */
public class UpdateStateTask extends RemoteStateTask<Boolean> {

    // Serialization id

    private static final long serialVersionUID = 1L;

    // File status

    private int m_fileStatus;

    /**
     * Default constructor
     */
    public UpdateStateTask() {
    }

    /**
     * Class constructor
     *
     * @param mapName
     *            String
     * @param key
     *            String
     * @param fileSts
     *            int
     * @param debug
     *            boolean
     * @param timingDebug
     *            boolean
     */
    public UpdateStateTask(final String mapName, final String key, final int fileSts, final boolean debug, final boolean timingDebug) {
        super(mapName, key, true, false, debug, timingDebug);

        m_fileStatus = fileSts;
    }

    /**
     * Run a remote task against a file state
     *
     * @param stateCache
     *            IMap<String, ClusterFileState>
     * @param fState
     *            ClusterFileState
     * @return Boolean
     * @exception Exception
     */
    @Override
    protected Boolean runRemoteTaskAgainstState(final IMap<String, ClusterFileState> stateCache, final ClusterFileState fState) throws Exception {

        // DEBUG

        if (hasDebug()) {
            Debug.println("UpdateStateTask: Update file status=" + FileStatus.asString(m_fileStatus) + ", state=" + fState);
        }

        // Check if the file status has changed

        boolean changedSts = false;

        if (fState.getFileStatus() != m_fileStatus) {

            // Update the file status

            fState.setFileStatusInternal(m_fileStatus, FileState.ReasonNone);
            changedSts = true;

            // If the status indicates the file/folder no longer exists then clear the file id, state attributes

            if (fState.getFileStatus() == FileStatus.NotExist) {

                // Reset the file id

                fState.setFileId(FileState.UnknownFileId);

                // Clear out any state attributes

                fState.removeAllAttributes();
            }

            // DEBUG

            if (hasDebug()) {
                Debug.println("UpdateStateTask: Status updated, state=" + fState);
            }
        }

        // Return a status

        return Boolean.valueOf(changedSts);
    }
}
