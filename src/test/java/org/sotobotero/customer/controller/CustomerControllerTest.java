package org.sotobotero.customer.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sotobotero.customer.entities.Customer;
import org.sotobotero.customer.repository.CustomerRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;


@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

	private static final String BASE_PATH = "/api/v1/customer";
	private static final Long CUSTOMER_ID = 1L;
	private static final String CUSTOMER_ID_AS_STRING = "1";
	private static final String CUSTOMER_NAME = "Jane Doe";
	private static final String UPDATED_NAME = "Jane Smith";
	private static final String MISSING_ID_AS_STRING = "99";

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private CustomerController customerController;

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
	}

	@Test
	void shouldReturnAllCustomers() throws Exception {
		when(customerRepository.findAll()).thenReturn(List.of(sampleCustomer(CUSTOMER_ID, CUSTOMER_NAME)));

		mockMvc.perform(get(BASE_PATH).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(1)))
				.andExpect(jsonPath("$[0].name", is(CUSTOMER_NAME)))
				.andExpect(jsonPath("$[0].email", is("jane@example.com")));
	}

	@Test
	void shouldReturnEmptyListWhenNoCustomersExist() throws Exception {
		when(customerRepository.findAll()).thenReturn(Collections.emptyList());

		mockMvc.perform(get(BASE_PATH).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void shouldReturnCustomerById() throws Exception {
		when(customerRepository.findById(CUSTOMER_ID_AS_STRING))
				.thenReturn(Optional.of(sampleCustomer(CUSTOMER_ID, CUSTOMER_NAME)));

		mockMvc.perform(get(BASE_PATH + "/{id}", CUSTOMER_ID).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is(CUSTOMER_NAME)));
	}

	@Test
	void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
		when(customerRepository.findById(MISSING_ID_AS_STRING)).thenReturn(Optional.empty());

		mockMvc.perform(get(BASE_PATH + "/{id}", 99L).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldCreateCustomer() throws Exception {
		Customer payload = sampleCustomer(null, CUSTOMER_NAME);
		Customer saved = sampleCustomer(CUSTOMER_ID, CUSTOMER_NAME);
		when(customerRepository.save(any(Customer.class))).thenReturn(saved);

		mockMvc.perform(post(BASE_PATH)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is(CUSTOMER_NAME)));
	}

	@Test
	void shouldUpdateCustomer() throws Exception {
		Customer existing = sampleCustomer(CUSTOMER_ID, CUSTOMER_NAME);
		Customer payload = sampleCustomer(CUSTOMER_ID, UPDATED_NAME);
		when(customerRepository.findById(CUSTOMER_ID_AS_STRING)).thenReturn(Optional.of(existing));
		when(customerRepository.save(any(Customer.class))).thenReturn(payload);

		mockMvc.perform(put(BASE_PATH)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is(UPDATED_NAME)));
	}

	@Test
	void shouldReturnNotFoundWhenUpdatingMissingCustomer() throws Exception {
		Customer payload = sampleCustomer(99L, UPDATED_NAME);
		when(customerRepository.findById(MISSING_ID_AS_STRING)).thenReturn(Optional.empty());

		mockMvc.perform(put(BASE_PATH)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isNotFound());
		verify(customerRepository, never()).save(any(Customer.class));
	}

	@Test
	void shouldUpdateCustomerName() throws Exception {
		Customer existing = sampleCustomer(CUSTOMER_ID, CUSTOMER_NAME);
		when(customerRepository.findById(CUSTOMER_ID_AS_STRING)).thenReturn(Optional.of(existing));
		when(customerRepository.save(any(Customer.class))).thenReturn(existing);

		mockMvc.perform(patch(BASE_PATH + "/{id}", CUSTOMER_ID)
						.param("name", UPDATED_NAME)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is(UPDATED_NAME)));
	}

	@Test
	void shouldReturnNotFoundWhenUpdatingNameOfMissingCustomer() throws Exception {
		when(customerRepository.findById(MISSING_ID_AS_STRING)).thenReturn(Optional.empty());

		mockMvc.perform(patch(BASE_PATH + "/{id}", 99L)
						.param("name", UPDATED_NAME)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
		verify(customerRepository, never()).save(any(Customer.class));
	}

	@Test
	void shouldDeleteCustomer() throws Exception {
		Customer existing = sampleCustomer(CUSTOMER_ID, CUSTOMER_NAME);
		when(customerRepository.findById(CUSTOMER_ID_AS_STRING)).thenReturn(Optional.of(existing));

		mockMvc.perform(delete(BASE_PATH + "/{id}", CUSTOMER_ID).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		verify(customerRepository).delete(existing);
	}

	@Test
	void shouldReturnNotFoundWhenDeletingMissingCustomer() throws Exception {
		when(customerRepository.findById(MISSING_ID_AS_STRING)).thenReturn(Optional.empty());

		mockMvc.perform(delete(BASE_PATH + "/{id}", 99L).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
		verify(customerRepository, never()).delete(any(Customer.class));
	}

	private Customer sampleCustomer(Long id, String name) {
		return new Customer(
				id,
				name,
				"jane@example.com",
				"123456789",
				"123 Main Street",
				"Anytown",
				"Anystate",
				"US",
				"12345",
				"ABC Inc.",
				"Engineer",
				"www.example.com",
				"twitter",
				"facebook",
				"linkedin",
				"github",
				"instagram",
				"youtube",
				"tiktok",
				"snapchat",
				"twitch",
				"other",
				"notes",
				34);
	}
}
