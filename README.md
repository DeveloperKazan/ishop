# ishop
ishop project technical description

Шаблон проектирования MVC

Web-компоненты используемые в проекте:

1.	Для обработки запросов пользователя реализовал классы-контроллеры в которых использовал возможности интерфейсов Servlet, Filter пакета javax.servlet. Servlet принимает запрос от пользователя, валидирует запрос, запрашивает модель из BusinessService, затем передает управление на View (JSP+JSTL) или другому контроллеру. 
2.	Для обработки ошибок реализовал класс ErrorHandlerFilter extends AbstractFilter который принимает запрос, валидирует и пропускает запрос дальше или передает управление на контроллер явно. Если возникает exception, то фильтр перехватывает ошибку и передает управление на контроллер, обрабатывающий ошибки.
3.	Для отображения данных использовал технологии JSP+JSTl.
Шаблонизация представления: <jsp:include page="${currentPage }" /> - тэг для подключения данных на JSP страницу, с записью в переменную страницы через контроллер.  Для добавления динамики в html страницу использовал библиотеку JSTL. 
page-template.jsp – общий шаблон страницы в которую динамически загружаются фрагменты из папки page и fragment с помощью AJAX-загрузки.
4.	Java POJO-классы представляют модель предметной области.
5.	Данные хранятся в БД PostgreSQL. Создал БД и реализовал класс TestDataGenerator для генерации тестовых данных и заполнения tables category, producer и product данными. Остальные таблицы будут заполняться после регистрации в БД, по мере заказа товаров 
6.	На уровне BusinessService использовал Java-классы + JDBC API для получения ResultSet и дальнейшего преобразования в модели Java и передачи моделей на уровень контроллера 
Папка entity хранит модели для БД. Папка model хранит модели не связанные с БД, такие как текущая корзина или авторизированный пользователь и т.д. Настройки окружения в application.properties. application.production=true – глобальная переменная проекта. logger-config.xml – для логирования действий. 
JSP-страницы в папке WEB-INF. Все запросы к JSP только через контроллер: 
index.jsp c редиректом на home страницу (список продуктов)


public final class RoutingUtils описывает правила передачи управления:

forwardToFragment(String jspFragment, req, resp) – передача управления на фрагмент

forwardToPage(String jspPage, req, resp) - передача управления на page-template.jsp с указанием в атрибуте currentPage пути к странице page/.

sendHTMLFragment(String text, req, resp){resp.getWriter().println(text)} – передача html-фрагмента с контроллера осуществляется методом интерфейса Response, resp.setContentType("text/html") – указанием контента и resp.getWriter().close()– закрытие потока

redirect(String url, req, resp){resp.sendRedirect(url)} – передача управления по указанному url

public abstract class AbstractController – хранит общие действия для всех классов-контроллеров

@WebServlet("/products")
public class AllProductsController extends AbstractController – отвечает за отображение всех продуктов. Получение коллекции продуктов из БД, установка коллекции в атрибут "products" запроса.
List<Product> products = getProductService().listAllProducts(1,Constants.MAX_PRODUCTS_PER_HTML_PAGE) – получаем коллекцию товаров из БД постранично, с максимальным количеством товаров на странице 12 (можно любое число кратное 12 для правильного отображения 1,2,4,6,12,24)

RoutingUtils.forwardToPage("products.jsp", req, resp) – передача управления на страницу jsp
Логика взаимодействия Java классов с БД

class ProductServiceImpl implements ProductService – класс содержит private static final объекты типа ResultSetHandler<List<Product>>, полученные из методов по соответствующим JDBCUtils.select запросам к БД. Таким образом результаты запросов к БД преобразуются в Java объекты.
Для установки соединения с БД в методе public List<Product> listAllProducts(int page, int limit) получаю Connection из объекта DataSource
Для реализации запросов к БД создал классы в пакете JDBC, которые способны выполнять подключение к БД и преобразовывать результаты запросов в Java объекты:   

public final class JDBCUtils {
public static <T> T select(Connection c, String sql, ResultSetHandler<T> resultSetHandler, Object... parameters) – в методе создаётся объект PreparedStatement, заполняется параметрами запроса, выполняется ResultSet rs = ps.executeQuery(), объект ResultSet передаётся в resultSetHandler

interface ResultSetHandler<T> - преобразует select запрос к БД (ResultSet) в соответствующий объект

class ResultSetHandlerFactory – задаёт правило формирования Product. В классе создаю параметризированный интерфейс ResultSetHandler<Product>, реализую метод public Product handle(ResultSet rs) в котором инициализирую поля класса Product

Если select запрос к БД должен вернуть коллекцию объектов, то вызываю метод static <T> ResultSetHandler<List<T>> getListResultSetHandler

В случае, если метод select должен мне вернуть единичный объект вызываю метод static <T> ResultSetHandler<T> getSingleResultSetHandler. Возвращает пустой List если коллекция пустая.

В блоке try(){}catch (SQLException e) – checked Exception преобразую в RuntimeException (unchecked), для того чтобы Exception передать по стеку на уровень 
class ErrorHandlerFilter
 
@WebFilter(filterName = "ErrorHandlerFilter")
public class ErrorHandlerFilter extends AbstractFilter – обрабатывает ошибки
chain.doFilter(req, new ThrowExceptionInsteadOfSendErrorResponse(resp)) – передаёт управление по цепочке фильтров, в случае возникновения ошибки if (th instanceof ValidationException) логирует событие LOGGER.warn("Request is not valid: " + th.getMessage()) и передаёт управление на error.jsp. Логгер проекта - Logback

abstract class AbstractFilter implements Filter – объединяет логику всех фильтров
а именно методы init(), destroy(), doFilter()

Доступ на уровень BusinessService (ServiceManager) имеют только классы-контроллеры (MVC). ИЗ JSP уровень BusinessService не доступен, так как JSP относяться к уровню представления (Vier)  

public class ServiceManager – класс для доступа к объектам BusinessService.
Бизнес сервис с настройками окружения существует в единственном объекте (Singleton), поэтому этот объект храниться в атрибутах ServletContext.
Доступ к Singleton объекту могут получать: слушатели приложения, слушатели сессии, сервлеты, фильтры, JSP страницы и JSP тэги.
ServiceManager instance = (ServiceManager)context.getAttribute("SERVICE_MANAGER") – instance объекта получаю из атрибута ServletContext, затем сохраняю объект по указанной ссылке в атрибут SERVICE_MANAGER context.setAttribute("SERVICE_MANAGER", instance). 
В конструкторе ServiceManager создаю объекты dataSource, productService, orderService, socialService - так как доступ к ним будет нужен из всех сервлетов вынес ссылки на эти объекты в abstract class AbstractController
 
dataSource.close() – закрывает открытое соединение с БД 

Доступ к объекту ServiceManager из слушателя
@WebListener
public class IShopApplicationListener implements ServletContextListener – слушатель получает ServiceManager instance при создании web-приложения.

@Override
public void contextInitialized(ServletContextEvent sce) – получаю instance объекта (Singleton), устанавливаю атрибуты CATEGORY_LIST, PRODUCER_LIST

@Override
public void contextDestroyed(ServletContextEvent sce) – при закрытии приложения
serviceManager.close() – закрывает все внешние ресурсы объектов класса serviceManager
(потоки ввода/вывода, открытые соединения с БД)

Доступ к объекту ServiceManager из сервлета
abstract class AbstractController extends HttpServlet
public final void init() – инициализация объектов ServiceManager (final – чтобы классы-наследники не переопределили этот метод)
productService = ServiceManager.getInstance(getServletContext()).getProductService() – объект (Singleton) получаем из ServiceManager

Доступ к объекту ServiceManager из объекта instance Filter получаем аналогично Servlet

Пример доступа к BusinessService из JSP (в проекте не использовал, так как противоречит MVC):

<%! private BusinessService businessService; %>
<%
if(businessService == null) {
businessService = ServiceManager.getInstance(application).getBusinessService();}
businessService.doSomething();
%>
Доступ из JSP-тэга (пример использования – считывание атрибута из переменной):
public class Tag extends TagSupport {
private BusinessService businessService;
@Override
public int doStartTag() throws JspException {
if(businessService == null) {
 businessService = ServiceManager.getInstance(
pageContext.getServletContext()).getBusinessService();
}
businessService.doSomething();
return SKIP_BODY;
}
@Override
public void release() {
super.release();
businessService = null;
}}

В пакете page создан класс для отображения продуктов по категориям: 
@WebServlet("/products/*")
public class ProductsByCategoryMoreController extends AbstractController
К URL "/products" добавил URL /*, где в качестве параметра будет указываться та категория, по которой будет выполняться поиск и фильтрация из БД. 
Для этого из текущего URL вырезаю "/products":
  String categoryUrl = req.getRequestURI().substring(SUBSTRING_INDEX)
То, что остаётся будет URL нужной категории
Далее получаю коллекцию продуктов List<Product> products, 
устанавливаю атрибуты и передаю управление на JSP – 
RoutingUtils.forwardToPage("products.jsp", req, resp)

@WebServlet("/ajax/html/more/search")
public class SearchResultsMoreController extends AbstractController – по URL /search создаёт объект SearchForm searchForm = createSearchForm(req), устанавливает атрибуты в запрос и переходит на search-result.jsp
В search-result.jsp подключил products.jsp, и установил сообщение <div class="alert alert-info"> в котором указываю найденное количество продуктов 
<p>Found <strong>${productCount }</strong> products</p>

Для каждого класса-контроллера создал AJAX-версию в пакете ajax. AJAX-загрузка будет выполняться при loadmore products, добавлении/удалении товара в корзину, отображении товара по категории

@WebServlet("/shopping-cart")
public class ShowShoppingCartController – переводит на shopping-cart.jsp в случае если CurrentShoppingCart существует, иначе переводит на /products.jsp

На странице products.jsp стиль кнопки преобразуется в loadmore и обратно в JS-коде - <a id="loadMore" class="btn btn-success">Load more products</a>


class TestDataGenerator – загрузка тестовых данных в БД. Класс в структуру Maven проекта не загружается (его нет в архиве war), но он компилируется в target. Класс содержит настройки подключения к БД. Соединение с БД устанавливается с помощью JDBC API и драйвера JDBC. Для каждой категории продуктов рандомно загружаются изображения из папки external.test-data
Для всех сущностей в БД создал соответствующие им классы, которые наследуются от параметризированного abstract class AbstractEntity<T> implements Serializable
Поле id установил в абстрактный класс так как id содержит все таблицы в БД
Сущность prise в БД имеет тип numeric(8,2), в соответствующем class Product это поле имеет тип BigDecimal (см. java.math.BigDecimal) для избегания погрешности при вычислении
class Order extends AbstractEntity<Long>
public BigDecimal getTotalCost() – высчитывает суммарную стоимость текущего заказа

Реализовал class ProductServiceImpl implements ProductService
