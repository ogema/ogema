/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.channels;

import org.ogema.core.channelmanager.ChannelAccessException;

/**
 * A ReaderThreadFactory creates ReaderThreads and assigns them to specific channels.
 * The policy which channel is assigned to which ReaderThread is up to the Factory.
 * 
 * @author pau
 *
 */
public interface ReaderThreadFactory {

	/**
	 * Callback by the ReaderThread that it has no active Channels left.
	 * The ReaderThread is now dormant until new Channels are added to it.
	 * The Factory may now terminated the ReaderThrea.
	 * 
	 * @param readerThread the ReaderThread that has active channels
	 */
	void noChannels(ReaderThread readerThread);
	
	/**
	 * Get a ReaderThread for the specific Channel. 
	 * This command also adds the Channel to the ReaderThread.
	 * Called by the Channel.
	 * 
	 * @param channelLocator
	 * @return ReaderThread for use with the channel
	 * @throws ChannelAccessException 
	 */
	ReaderThread getReaderThread(Channel channel) throws ChannelAccessException;
	
}
