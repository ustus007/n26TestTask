package test.n26.controllers;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import test.n26.components.CurrentStats;
import test.n26.components.TransactionRequest;
import test.n26.managers.infc.GeneralManager;

/**
 * 
 * @author Denys Nikolskyy
 *
 * Controller class for all required endpoints
 *
 */
@RestController
public class MainController {

	@Autowired
	GeneralManager generalManager;


	/**
	 * 
	 * Endpoint for creating transactions
	 * 
	 * @param rb representation of json-formed request body
	 * @param response response object for setting a response status
	 * @return Callable to execute asynchronously
	 */
	
	@RequestMapping(value = "/transactions", method = RequestMethod.POST)
	public Callable<String> addTransaction(@RequestBody TransactionRequest rb, HttpServletResponse response) {
		return () -> {
			boolean result = generalManager.saveTransaction(rb);
			if (result) {
				response.setStatus(201);
			} else {
				response.setStatus(204);
			}
			return ""; //ensure that response body is empty
		};
	}
	
	/**
	 * 
	 * Endpoint for getting statistics information
	 * 
	 * @return Callable to execute asynchronously
	 */
	
	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	public @ResponseBody Callable<CurrentStats> getStatistics(){
		return () -> {
			CurrentStats result = generalManager.getStats();
			return result;
		};
	}
	

}