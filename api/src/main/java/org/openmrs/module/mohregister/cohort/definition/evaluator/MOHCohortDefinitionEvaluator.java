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

package org.openmrs.module.mohregister.cohort.definition.evaluator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohregister.PropertyNameConstants;
import org.openmrs.module.mohregister.cohort.definition.MOHCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;

@Handler(supports = {MOHCohortDefinition.class})
public class MOHCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

	private static final Log log = LogFactory.getLog(MOHCohortDefinitionEvaluator.class);

	public static final String ENCOUNTER_TYPE_ADULT_RETURN = "ADULTRETURN";

	public static final String ENCOUNTER_TYPE_ADULT_INITIAL = "ADULTINITIAL";

	public static final String FIRST_HIV_RAPID_TEST_QUALITATIVE_CONCEPT = "HIV RAPID TEST, QUALITATIVE";

	public static final String SECOND_HIV_RAPID_TEST_QUALITATIVE_CONCEPT = "HIV RAPID TEST 2, QUALITATIVE";

	public static final String POSITIVE_CONCEPT = "POSITIVE";

	public static final String HIV_ENZYME_IMMUNOASSAY_QUALITATIVE_CONCEPT = "HIV ENZYME IMMUNOASSAY, QUALITATIVE";

	public EvaluatedCohort evaluate(final CohortDefinition cohortDefinition, final EvaluationContext evaluationContext) throws EvaluationException {

		EncounterService service = Context.getEncounterService();
		ConceptService conceptService = Context.getConceptService();
		CohortDefinitionService definitionService = Context.getService(CohortDefinitionService.class);

		EncounterCohortDefinition encounterCohortDefinition = new EncounterCohortDefinition();
		encounterCohortDefinition.addEncounterType(service.getEncounterType(ENCOUNTER_TYPE_ADULT_INITIAL));
		encounterCohortDefinition.addEncounterType(service.getEncounterType(ENCOUNTER_TYPE_ADULT_RETURN));

		Cohort encounterCohort = definitionService.evaluate(encounterCohortDefinition, evaluationContext);

		Concept firstRapidConcept = conceptService.getConcept(FIRST_HIV_RAPID_TEST_QUALITATIVE_CONCEPT);
		Concept secondRapidConcept = conceptService.getConcept(SECOND_HIV_RAPID_TEST_QUALITATIVE_CONCEPT);
		Concept positiveConcept = conceptService.getConcept(POSITIVE_CONCEPT);

		CodedObsCohortDefinition firstRapidCohortDefinition = new CodedObsCohortDefinition();
		firstRapidCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
		firstRapidCohortDefinition.setQuestion(firstRapidConcept);
		firstRapidCohortDefinition.setOperator(SetComparator.IN);
		firstRapidCohortDefinition.setValueList(Arrays.asList(positiveConcept));

		CodedObsCohortDefinition secondRapidCohortDefinition = new CodedObsCohortDefinition();
		secondRapidCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
		secondRapidCohortDefinition.setQuestion(secondRapidConcept);
		secondRapidCohortDefinition.setOperator(SetComparator.IN);
		secondRapidCohortDefinition.setValueList(Arrays.asList(positiveConcept));

		CompositionCohortDefinition rapidCompositionCohortDefinition = new CompositionCohortDefinition();
		rapidCompositionCohortDefinition.addSearch("PositiveFirstRapid", firstRapidCohortDefinition, null);
		rapidCompositionCohortDefinition.addSearch("PositiveSecondRapid", secondRapidCohortDefinition, null);
		rapidCompositionCohortDefinition.setCompositionString("PositiveFirstRapid OR PositiveSecondRapid");

		Cohort rapidCompositionCohort = definitionService.evaluate(rapidCompositionCohortDefinition, evaluationContext);

		AgeCohortDefinition ageCohortDefinition = new AgeCohortDefinition();
		ageCohortDefinition.setMinAge(18);
		ageCohortDefinition.setMinAgeUnit(DurationUnit.MONTHS);
		ageCohortDefinition.setMinAge(14);
		ageCohortDefinition.setMinAgeUnit(DurationUnit.YEARS);

		Concept elisaConcept = conceptService.getConcept(HIV_ENZYME_IMMUNOASSAY_QUALITATIVE_CONCEPT);

		CodedObsCohortDefinition elisaCohortDefinition = new CodedObsCohortDefinition();
		elisaCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
		elisaCohortDefinition.setQuestion(elisaConcept);
		elisaCohortDefinition.setOperator(SetComparator.IN);
		elisaCohortDefinition.setValueList(Arrays.asList(positiveConcept));

		CompositionCohortDefinition elisaCompositionCohortDefinition = new CompositionCohortDefinition();
		elisaCompositionCohortDefinition.addSearch("PaediatricAge", ageCohortDefinition, null);
		elisaCompositionCohortDefinition.addSearch("PositiveElisa", elisaCohortDefinition, null);
		elisaCompositionCohortDefinition.setCompositionString("PaediatricAge AND PositiveElisa");

		Cohort elisaCompositionCohort = definitionService.evaluate(elisaCompositionCohortDefinition, evaluationContext);

		Map<String, Collection<OpenmrsObject>> restrictions = new HashMap<String, Collection<OpenmrsObject>>();
		restrictions.put(PropertyNameConstants.OBS_CONCEPT, Arrays.<OpenmrsObject>asList(elisaConcept));
		restrictions.put(PropertyNameConstants.OBS_VALUE_CODED, Arrays.<OpenmrsObject>asList(positiveConcept));

		Set<Integer> patientIds = new HashSet<Integer>();
		patientIds.addAll(encounterCohort.getMemberIds());
		patientIds.addAll(rapidCompositionCohort.getMemberIds());
		patientIds.addAll(elisaCompositionCohort.getMemberIds());

		return new EvaluatedCohort(new Cohort(patientIds), cohortDefinition, evaluationContext);
	}
}
