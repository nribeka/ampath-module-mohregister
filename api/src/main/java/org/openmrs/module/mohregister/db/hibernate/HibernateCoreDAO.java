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

package org.openmrs.module.mohregister.db.hibernate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohregister.db.CoreDAO;

public class HibernateCoreDAO implements CoreDAO{

	private static final Log log = LogFactory.getLog(HibernateCoreDAO.class);

	private SessionFactory sessionFactory;

	/**
	 * Method that will be called by Spring to inject the Hibernate's SessionFactory.
	 *
	 * @param sessionFactory the session factory to be injected
	 */
	public void setSessionFactory(final SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	public List<Encounter> getPatientEncounters(final Integer patientId, final Map<String, Collection<OpenmrsObject>> restrictions) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.add(Restrictions.eq("personId", patientId));

		criteria.addOrder(Order.desc("obsDatetime"));

		Obs observation = new Obs();
		for (String property : restrictions.keySet()) {
			Collection<OpenmrsObject> propertyValues = restrictions.get(property);
			if (CollectionUtils.isNotEmpty(propertyValues) && PropertyUtils.isReadable(observation, property)) {
				criteria.add(Restrictions.in(property, propertyValues));
				criteria.addOrder(Order.asc(property));
			}
		}

		criteria.add(Restrictions.eq("voided", Boolean.FALSE));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	public List<Obs> getPatientObservations(final Integer patientId, final Map<String, Collection<OpenmrsObject>> restrictions) throws DAOException {
		// create a hibernate criteria on the encounter object
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
		// restrict the encounter that will be returned to specific patient only
		criteria.add(Restrictions.eq("patientId", patientId));

		// default ordering for the returned encounter is descending on encounter datetime
		criteria.addOrder(Order.desc("encounterDatetime"));

		// create a dummy encounter object
		Encounter encounter = new Encounter();
		// iterate over each property in the restriction map
		for (String property : restrictions.keySet()) {
			// get the actual object that will restrict the encounter. this will contains the list of encounter type or list of location
			// or list of provider (currently not being used) passed from caller
			Collection<OpenmrsObject> propertyValues = restrictions.get(property);
			// check to make sure the list is not empty and the property is readable. example of the property for encounter are
			// encounterType or location of the encounter
			if (CollectionUtils.isNotEmpty(propertyValues) && PropertyUtils.isReadable(encounter, property)) {
				// create a restriction on the property with the above list as the value
				criteria.add(Restrictions.in(property, propertyValues));
				// add ordering on that property to prevent slowness when ordering only on encounter datetime (1.6.x only)
				criteria.addOrder(Order.asc(property));
			}
		}

		// exclude all voided encounters
		criteria.add(Restrictions.eq("voided", Boolean.FALSE));
		return criteria.list();
	}
}
