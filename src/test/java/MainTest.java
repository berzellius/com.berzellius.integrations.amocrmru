import com.berzellius.integrations.amocrmru.dto.ErrorHandlers.AmoCRMAPIRequestErrorHandler;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMContact;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMEntities;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.AmoCRMLead;
import com.berzellius.integrations.amocrmru.dto.api.amocrm.response.AmoCRMCreatedEntityResponse;
import com.berzellius.integrations.amocrmru.service.AmoCRMService;
import com.berzellius.integrations.amocrmru.service.AmoCRMServiceImpl;
import com.berzellius.integrations.basic.exception.APIAuthException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
     * удаление сделок через API не предусмотрено
     * @throws APIAuthException
     */
    //@Test
    public void testPostMethod() throws Exception {
        AmoCRMLead amoCRMLead = new AmoCRMLead();

        Date dt = new Date();
        String leadName = "Тестовая сделка " + dt.getTime();
        amoCRMLead.setName(leadName);

        /*ArrayList<AmoCRMTag> tags = new ArrayList<>();
        AmoCRMTag tag = new AmoCRMTag();
        tag.setName("sometag");
        AmoCRMTag tag2 = new AmoCRMTag();
        tag2.setName("другой тег");
        tags.add(tag);
        tags.add(tag2);
        amoCRMLead.setTags(tags);*/
        amoCRMLead.tag(1l, "этот тег");
        amoCRMLead.tag(2l, "тот тег");

        AmoCRMCreatedEntityResponse amoCRMCreatedEntityResponse = this.getAmoCRMService().addLead(amoCRMLead);
        Long idCreatedLead = amoCRMCreatedEntityResponse.getId();

        Assert.notNull(idCreatedLead);

        System.out.println("created lead#" + idCreatedLead);

        AmoCRMLead createdLead = this.getAmoCRMService().getLeadById(idCreatedLead);
        createdLead.setPrice(BigDecimal.ONE);

        AmoCRMEntities entities = new AmoCRMEntities();
        ArrayList<AmoCRMLead> leadsToEdit = new ArrayList<>();
        leadsToEdit.add(createdLead);
        entities.setUpdate(leadsToEdit);
        this.getAmoCRMService().editLeads(entities);
    }

    public void setAmoCRMService(AmoCRMService amoCRMService) {
        this.amoCRMService = amoCRMService;
    }

    public AmoCRMService getAmoCRMService() {
        return amoCRMService;
    }
}
