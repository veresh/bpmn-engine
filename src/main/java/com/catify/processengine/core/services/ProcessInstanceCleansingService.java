/**
 * -------------------------------------------------------
 * Copyright (C) 2013 catify <info@catify.com>
 * -------------------------------------------------------
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package com.catify.processengine.core.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.UntypedActor;

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.messages.ArchiveMessage;
import com.catify.processengine.core.messages.DeletionMessage;

/**
 * The ProcessInstanceCleansingService either deletes or archives a process instance asynchronously.
 * 
 * @author chris
 */
@Configurable
public class ProcessInstanceCleansingService extends UntypedActor {

	@Autowired
	private ProcessInstanceMediatorService processInstanceMediatorService;
	
	private DataObjectService dataObjectService = new DataObjectService();
	
	public ProcessInstanceCleansingService() {
		
	}

	/**
	 * On receiving of a MetaDataMessage this actor will write the meta data to
	 * the process instance node
	 */
	@Override
	public void onReceive(Object message) {
		
		// the handling of the data objects saved by a process instance must be evaluated, see redmine #113
		if (message instanceof ArchiveMessage) {
			processInstanceMediatorService.archiveProcessInstance(
					((ArchiveMessage) message).getUniqueProcessId(),
					((ArchiveMessage) message).getProcessInstanceId(), 
					((ArchiveMessage) message).getEndTime());
		} else if (message instanceof DeletionMessage) {
			processInstanceMediatorService.deleteProcessInstance(
					((DeletionMessage) message).getUniqueProcessId(),
					((DeletionMessage) message).getProcessInstanceId());
			
			for (String dataObjectId : ((DeletionMessage) message).getDataObjectIds()) {
				this.getDataObjectService().deleteObject(((DeletionMessage) message).getUniqueProcessId(), 
						dataObjectId, ((DeletionMessage) message).getProcessInstanceId());
			}
		} else {
			unhandled(message);
		}
	}

	/**
	 * Gets the data object service.
	 *
	 * @return the data object service
	 */
	public DataObjectService getDataObjectService() {
		return dataObjectService;
	}

	/**
	 * Sets the data object service.
	 *
	 * @param dataObjectService the new data object service
	 */
	public void setDataObjectService(DataObjectService dataObjectService) {
		this.dataObjectService = dataObjectService;
	}

}
