package com.routeanalyzer.api.logic.impl;

import com.routeanalyzer.api.logic.LapsOperations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringJUnit4ClassRunner.class)
public class ActivityOperationsImplTest {

	@Mock
	private LapsOperations lapsOperations;
	@InjectMocks
	private ActivityOperationsImpl activityOperations;

	@Before
	public void setUp() {

	}

	@Test
	public void uploadTCXFileTest() {

	}



}
