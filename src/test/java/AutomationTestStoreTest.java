import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AutomationTestStoreTest {

  private static final String LOGIN_XPATH             = "//*[@id=\"customer_menu_top\"]/li";
  private static final String SEARCH_ICON_XPATH       = "//*[@id=\"search_form\"]/div/div/i";
  private static final String SUBMIT_LOGIN_XPATH      = "//*[@id=\"loginFrm\"]/fieldset/button";
  private static final String ITEM_XPATH              =
      "//*[@id=\"maincontainer\"]/div/div/div/div/div[3]/div[3]/div[1]/div/a";
  private static final String ORDER_NUMBER_TEXT_XPATH = "//*[@id=\"maincontainer\"]/div/div/div/div/section/p[2]";
  private static final String ORDER_DETAILS_XPATH     =
      "//*[@id=\"maincontainer\"]/div/div[1]/div/div/div[2]/div/div[1]/div/a/i";
  private static final String ORDERS_LIST_SELECTOR    = ".container-fluid.mt20";
  private static final String MENU_TEXT_CLASS_NAME    = ".menu_text";
  private static final String CART_CLASS_NAME         = ".cart";
  private static final String CART_CHECKOUT_ID        = "cart_checkout1";
  private static final String CONFIRM_CHECKOUT_ID     = "checkout_btn";
  private static final String PRICE_CLASS_NAME        = ".productfilneprice";
  private static final String TOTAL_PRICE_CLASS_NAME  = ".total-price";
  private static final String SEARCH_CLASS_NAME       = "filter_keyword";
  private static final String LOGIN_CLASS_NAME        = "loginname";
  private static final String PASSWORD_CLASS_NAME     = "password";
  private static final String QUANTITY_CLASS_NAME     = "quantity";
  private static final String BASE_URL                = "https://automationteststore.com/";
  private static final String ITEM_NAME               = "Casual 3/4 Sleeve Baseball T-Shirt";
  private static final String QUANTITY_OF_PRODUCT     = "5";
  private static final String LOGIN_USERNAME          = "VaspurVaspuryan";
  private static final String LOGIN_PASSWORD          = "123456789";

  private static WebDriver webDriver;

  @BeforeAll
  public static void setup() {
    webDriver = new ChromeDriver();
  }

  @AfterAll
  public static void clean() {
    webDriver.close();
  }

  @Test
  public void shouldSuccessfullyPlaceOrder() {
    final WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(1));

    webDriver.get(BASE_URL);
    webDriver.manage().window().maximize();

    webDriver.findElement(By.xpath(LOGIN_XPATH)).click();
    webDriver.findElement(By.name(LOGIN_CLASS_NAME)).sendKeys(LOGIN_USERNAME);
    webDriver.findElement(By.name(PASSWORD_CLASS_NAME)).sendKeys(LOGIN_PASSWORD);
    webDriver.findElement(By.xpath(SUBMIT_LOGIN_XPATH)).click();
    webDriver.findElement(By.name(SEARCH_CLASS_NAME)).sendKeys(ITEM_NAME);
    webDriver.findElement(By.xpath(SEARCH_ICON_XPATH)).click();
    webDriver.findElement(By.xpath(ITEM_XPATH)).click();

    final BigDecimal price = new BigDecimal(getPrice(PRICE_CLASS_NAME));

    webDriver.findElement(By.name(QUANTITY_CLASS_NAME)).clear();
    webDriver.findElement(By.name(QUANTITY_CLASS_NAME)).sendKeys(QUANTITY_OF_PRODUCT);

    webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(TOTAL_PRICE_CLASS_NAME)));

    final BigDecimal totalPrice = new BigDecimal(getPrice(TOTAL_PRICE_CLASS_NAME));

    verifyTotalPrice(price, totalPrice);

    webDriver.findElement(By.cssSelector(CART_CLASS_NAME)).click();
    webDriver.findElement(By.id(CART_CHECKOUT_ID)).click();
    webDriver.findElement(By.id(CONFIRM_CHECKOUT_ID)).click();

    webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(ORDER_NUMBER_TEXT_XPATH)));

    final String orderNumber = getOrderNumber();

    webDriver.findElement(By.cssSelector(MENU_TEXT_CLASS_NAME)).click();
    webDriver.findElement(By.xpath(ORDER_DETAILS_XPATH)).click();

    final List<WebElement> elements = webDriver.findElements(By.cssSelector(ORDERS_LIST_SELECTOR));

    final List<String> list = elements
        .stream()
        .map(WebElement::getText)
        .map(this::getOrderNumberFromText)
        .filter(orderNumber::equalsIgnoreCase)
        .toList();

    assertThat(list)
        .asInstanceOf(InstanceOfAssertFactories.list(String.class))
        .singleElement()
        .isEqualTo(orderNumber);
  }

  private String getOrderNumber() {
    return Optional
        .of(webDriver)
        .map(webDriver1 -> webDriver1.findElement(By.xpath(ORDER_NUMBER_TEXT_XPATH)))
        .map(WebElement::getText)
        .map(this::getOrderNumberFromText)
        .orElseThrow();
  }

  private String getPrice(final String selector) {
    return Optional
        .of(webDriver)
        .map(webDriver1 -> webDriver1.findElement(By.cssSelector(selector)))
        .map(WebElement::getText)
        .map(fullPrice -> fullPrice.substring(1))
        .orElseThrow();
  }

  private String getOrderNumberFromText(final String orderNumber) {
    return orderNumber.substring(orderNumber.indexOf("#") + 1, orderNumber.indexOf("#") + 6);
  }

  private void verifyTotalPrice(final BigDecimal price, final BigDecimal totalPrice) {
    assertThat(totalPrice)
        .isNotNull()
        .isEqualTo(price.multiply(new BigDecimal(QUANTITY_OF_PRODUCT)));
  }

}
