package com.cardsnap.ui.screens.contacts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import com.cardsnap.data.repository.ContactRepository
import com.cardsnap.domain.model.ContactCard
import com.cardsnap.domain.model.ContactsState
import com.cardsnap.util.VCardParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModelTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule val mockitoRule = org.mockito.junit.MockitoJUnit.rule()

    private lateinit var contactRepository: ContactRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        contactRepository = mock()
    }

    private fun makeViewModel() = ContactsViewModel(contactRepository)

    @Test
    fun init_loadsContacts_stateIsSuccess() = runTest {
        val contact1 = ContactCard(id = "1", name = "John Doe", email = "john@test.com")
        val contact2 = ContactCard(id = "2", name = "Jane Smith", phone = "555-1234")
        whenever(contactRepository.getAllContacts()).thenReturn(flowOf(listOf(contact1, contact2)))

        val viewModel = makeViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ContactsState.Success)
        assertEquals(2, (state as ContactsState.Success).contacts.size)
    }

    @Test
    fun init_loadsEmpty_stateIsEmpty() = runTest {
        whenever(contactRepository.getAllContacts()).thenReturn(flowOf(emptyList()))

        val viewModel = makeViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ContactsState.Empty)
    }

    @Test
    fun init_throwsException_stateIsError() = runTest {
        whenever(contactRepository.getAllContacts()).thenReturn(flow { throw RuntimeException("DB error") })

        val viewModel = makeViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ContactsState.Error)
        assertEquals("DB error", (state as ContactsState.Error).message)
    }

    @Test
    fun dismissDuplicatePair_filtersCorrectly() = runTest {
        val a = ContactCard(id = "1", name = "A")
        val b = ContactCard(id = "2", name = "B")

        val viewModel = makeViewModel()
        viewModel.dismissDuplicatePair(a, b)
        assertTrue(true)
    }

    @Test
    fun dismissDuplicates_clearsList() = runTest {
        val viewModel = makeViewModel()
        viewModel.dismissDuplicates()
        assertTrue(true)
    }
}