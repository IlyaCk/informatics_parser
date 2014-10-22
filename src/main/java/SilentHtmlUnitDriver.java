import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class SilentHtmlUnitDriver extends HtmlUnitDriver {
    public SilentHtmlUnitDriver() {
        super();
        this.getWebClient().setCssErrorHandler(new SilentCssErrorHandler());
    }
}