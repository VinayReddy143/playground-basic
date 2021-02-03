package com.fhir.querying;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

public class SampleClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleClient.class);
	private static final String FHIR_BASE_URL = "http://hapi.fhir.org/baseR4";

	public static void main(String[] theArgs) throws IOException, InterruptedException {
		// Create a FHIR client
		doBasicTask();
		doIntermediateTask();

	}

	private static void doBasicTask() {
		FhirContext fhirContext = FhirContext.forR4();
		IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_BASE_URL);
		client.registerInterceptor(new LoggingInterceptor(false));
		// Search for Patient resources
		Bundle response = client.search().forResource("Patient").where(Patient.FAMILY.matches().value("SMITH"))
				.returnBundle(Bundle.class).execute();
		LOGGER.info("================ Basic Task Start====================");
		printRequiredDetails(response.getEntry());
		// To Sort based on firstName, uncomment below line
//		List<BundleEntryComponent> sortedList = sortByFirstName(response.getEntry());
//		printRequiredDetails(sortedList);
		LOGGER.info("================ Basic Task End====================");
	}

	private static void doIntermediateTask() {
		LOGGER.info("================ Intermediate Task Start====================");
		try {
			String filePath = "Lastnames.txt";
			LOGGER.info("================ Iteration 1====================");
			searchNames(filePath, false);
			Thread.sleep(5000);
			LOGGER.info("================ Iteration 2====================");
			searchNames(filePath, false);
			Thread.sleep(5000);
			LOGGER.info("================ Iteration 3====================");
			searchNames(filePath, true);
		} catch (InterruptedException interruptedException) {
			LOGGER.error("InterruptedException occured", interruptedException);
		} catch (IOException ioException) {
			LOGGER.error("IOException occured", ioException);
		}
		LOGGER.info("================ Intermediate Task End====================");
	}

	public static void printRequiredDetails(List<BundleEntryComponent> list) {
		for (BundleEntryComponent bundleEntryComponent : list) {
			Resource resource = bundleEntryComponent.getResource();
			Property nameProperty = resource.getNamedProperty("name");
			Property dateOfBirthProperty = resource.getNamedProperty("birthDate");
			List<Base> namePropertyValues = nameProperty.getValues();
			for (Base base : namePropertyValues) {
				Property familyProperty = base.getNamedProperty("family");
				Property givenProperty = base.getNamedProperty("given");

				List<Base> givenPropertyValues = givenProperty.getValues();
				if (!givenPropertyValues.isEmpty() && !familyProperty.getValues().isEmpty()) {
					LOGGER.info(familyProperty.getValues().get(0).toString() + ", "
							+ givenPropertyValues.get(0).toString() + "," + dateOfBirthProperty.getValues().toString());
				}
			}
		}
	}

	public static List<BundleEntryComponent> sortByFirstName(List<BundleEntryComponent> list) {
		List<BundleEntryComponent> sortedList = list.stream()
				.sorted(Comparator
						.comparing(
								c -> c.getResource().getNamedProperty("name").getValues().get(0)
										.getNamedProperty("given").getValues().get(0).toString(),
								String.CASE_INSENSITIVE_ORDER))
				.collect(Collectors.toList());
		return sortedList;
	}

	/**
	 * This method searches calls SFHIR API and searches names mentioned in file.
	 * send disableCache value as true if cache needs to be disabled
	 * 
	 * @param filePath
	 * @param disableCache
	 * @throws IOException
	 */
	private static void searchNames(String filePath, Boolean disableCache) throws IOException {
		FileInputStream fstream = null;
		BufferedReader br = null;
		try {
			FhirContext fhirContext = FhirContext.forR4();
			fstream = new FileInputStream(filePath);
			br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			CacheControlDirective cacheControlDirective = new CacheControlDirective();
			cacheControlDirective.setNoCache(disableCache);
			ClientInterceptorImpl clientInterceptorImpl1 = new ClientInterceptorImpl();
			clientInterceptorImpl1.setResponseTimes(new ArrayList<>());
			IGenericClient genericClient = fhirContext.newRestfulGenericClient(FHIR_BASE_URL);
			genericClient.registerInterceptor(clientInterceptorImpl1);
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				Bundle bundle = genericClient.search().forResource("Patient").cacheControl(cacheControlDirective)
						.where(Patient.FAMILY.matches().value(strLine)).returnBundle(Bundle.class).execute();
//				printRequiredDetails(bundle.getEntry());
			}

			OptionalDouble average1 = clientInterceptorImpl1.getResponseTimes().stream().mapToDouble(a -> a).average();
			LOGGER.info("Average response time: " + average1.getAsDouble());
		} catch (FileNotFoundException fileNotFoundException) {
			LOGGER.error("fileNotFoundException occured", fileNotFoundException);
		} catch (IOException ioException) {
			LOGGER.error("IOException occured", ioException);
		} finally {
			if (fstream != null)
				fstream.close();
			if (br != null)
				br.close();
		}

	}
}
