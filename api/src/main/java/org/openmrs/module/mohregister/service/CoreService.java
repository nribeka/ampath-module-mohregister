/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.mohregister.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.util.PrivilegeConstants;

public interface CoreService {

	/**
	 * Get all patient observations that match the encounter , locations , concept and value coded criteria
	 *
	 * @param patientId    the patient
	 * @param restrictions list of all possible restrictions for observations
	 * @return all observations that match the criteria or empty list when no observations match the criteria
	 * @throws APIException
	 * @should return all observations that match the search criteria
	 * @should return empty list when no observation match the criteria
	 */
	@Authorized({PrivilegeConstants.VIEW_OBS})
	List<Obs> getPatientObservations(final Integer patientId, final Map<String, Collection<OpenmrsObject>> restrictions) throws APIException;

	/**
	 * Get all patient encounters that match the encounter types, locations and providers criteria
	 *
	 * @param patientId    the patient
	 * @param restrictions list of all possible restrictions for encounters
	 * @return all encounters that match the criteria or empty list when no encounters match the criteria
	 * @throws APIException
	 * @should return all encounters that match the search criteria
	 * @should return empty list when no encounter match the criteria
	 */
	@Authorized({PrivilegeConstants.VIEW_ENCOUNTERS})
	List<Encounter> getPatientEncounters(final Integer patientId, final Map<String, Collection<OpenmrsObject>> restrictions) throws APIException;

}
