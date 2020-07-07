package net.ishop.service;

import java.util.List;

import net.ishop.entity.Category;
import net.ishop.entity.Producer;
import net.ishop.entity.Product;
import net.ishop.form.SearchForm;

public interface ProductService {

	List<Product> listAllProducts(int page, int limit);
	
	int countAllProducts();
	
	List<Product> listProductsByCategory(String categoryUrl, int page, int limit);
	
	int countProductsByCategory(String categoryUrl);
	
	List<Category> listAllCategories();
	
	List<Producer> listAllProducers();
	
	List<Product> listProductsBySearchForm(SearchForm searchForm, int page, int limit);
	
	int countProductsBySearchForm(SearchForm searchForm);
}
