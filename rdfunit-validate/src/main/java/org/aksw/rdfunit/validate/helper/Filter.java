package org.aksw.rdfunit.validate.helper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.rdfunit.model.interfaces.TestCase;

import org.aksw.rdfunit.model.interfaces.TestSuite;
import org.aksw.rdfunit.statistics.DatasetStatisticsClasses;
import org.aksw.rdfunit.statistics.DatasetStatisticsProperties;
import org.apache.jena.ext.com.google.common.collect.ImmutableSet;

public class Filter {
	
	public static TestSuite filterTestSuiteForDataset(TestSuite testSuite, QueryExecutionFactory qef) {
	
        final Set<String> classAndProperties =
        		ImmutableSet.of(new DatasetStatisticsClasses(), new DatasetStatisticsProperties()).stream()
                .flatMap(s -> s.getStatisticsMap(qef).keySet().stream())
                .distinct()
               // .filter(s -> excludeNs.stream().noneMatch(s::startsWith))
                .flatMap(s -> getNamespaces(s).stream())
                .collect(Collectors.toSet());
		
		List<TestCase> filteredTestCases = testSuite.getTestCases().stream()
                .filter(t -> {
                    Collection<String> annotations = t.getTestCaseAnnotation().getReferences();
                    // match test if there are no metadata (can e.g. be global)
                    // or if the entities it references match with the dataset classes and properties
                    if (annotations.isEmpty()) {
                        return true;
                    } else {
                        return annotations.stream()
                                .anyMatch(classAndProperties::contains);
                    }
                })
                .collect(Collectors.toList());
		
		return new TestSuite(filteredTestCases);
	}
	
	private static final ImmutableSet<Character> delimiters = ImmutableSet.of('#', '/', ':');
	
	private static Set<String> getNamespaces(String iri) {
        ImmutableSet.Builder<String> nss = ImmutableSet.builder();


        nss.add(iri);

        int posAfterHttp = 8;
        for (int i = posAfterHttp; i< iri.length(); i++) {
            if (delimiters.contains(iri.charAt(i))) {
                nss.add(iri.substring(0, i));
                nss.add(iri.substring(0, i+1));
            }
        }

        return nss.build();
    }

}
