/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	 * @param channel
	 * @return ReaderThread for use with the channel
	 * @throws ChannelAccessException 
	 */
	ReaderThread getReaderThread(Channel channel) throws ChannelAccessException;
	
}
