import com.berzellius.integrations.amocrmru.dto.ErrorHandlers.AmoCRMAPIRequestErrorHandler;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMContact;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMLead;
import com.berzellius.integrations.amocrmru.service.AmoCRMService;
import com.berzellius.integrations.amocrmru.service.AmoCRMServiceImpl;
import com.berzellius.integrations.basic.exception.APIAuthException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by berz on 08.01.2017.
 */
public class MainTest {

    private AmoCRMService amoCRMService;

    @Before
    public void setup(){
        AmoCRMService amoCRMService1 = new AmoCRMServiceImpl();
        amoCRMService1.setApiBaseUrl(TestAPI.AmoCRMApiBaseUrl);
        amoCRMService1.setLoginUrl(TestAPI.AmoCRMLoginUrl);
        amoCRMService1.setUserHash(TestAPI.AmoCRMHash);
        amoCRMService1.setUserLogin(TestAPI.AmoCRMUser);

        AmoCRMAPIRequestErrorHandler errorHandler = new AmoCRMAPIRequestErrorHandler();
        amoCRMService1.setErrorHandler(errorHandler);

        this.setAmoCRMService(amoCRMService1);
    }

    @Test
    public void simpleTest() throws APIAuthException {
        List<AmoCRMContact> crmContacts = this.getAmoCRMService().getContactsByQuery("");

        System.out.println(crmContacts);

        List<AmoCRMLead> crmLeads = this.getAmoCRMService().getLeadsByQuery("");

        System.out.println(crmLeads);
    }

    /**
     * Тест создания сделки
     * ВНИМАНИЕ! Тест создает сделку, поэтому аннотацию @Test стоит держать закомментированной
     * todo дописать удаление сделки, тогда результат можно будеть видеть в корзине, никому не мешая. Выводить id созданной сделки в лог
     * @throws APIAuthException
     */
    //@Test
    public void testPostMethod() throws APIAuthException {
        AmoCRMLead amoCRMLead = new AmoCRMLead();
        amoCRMLead.setName("Тестовая сделка");

        this.getAmoCRMService().addLead(amoCRMLead);
    }

    public void setAmoCRMService(AmoCRMService amoCRMService) {
        this.amoCRMService = amoCRMService;
    }

    public AmoCRMService getAmoCRMService() {
        return amoCRMService;
    }
}
