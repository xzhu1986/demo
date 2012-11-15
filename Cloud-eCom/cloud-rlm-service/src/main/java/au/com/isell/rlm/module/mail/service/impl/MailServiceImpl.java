package au.com.isell.rlm.module.mail.service.impl;

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.exception.BizAssert;
import au.com.isell.rlm.module.mail.service.MailService;
import au.com.isell.rlm.module.mail.vo.MailContent;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author Frank Wu 24/05/2011
 */
@Service("mailService")
public class MailServiceImpl implements MailService {
	private static Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

	private static final String MAIL_FROM_USERNAME = "iData System Notifications";
	public static final String CHARSET = "UTF-8";

	@Value("${mail.smtp.server}")
	private String smtpServer;
	@Value("${mail.address}")
	private String mailAddress;
	@Value("${mail.smtp.user}")
	private String mailUser;
	@Value("${mail.smtp.pwd}")
	private String mailPwd;
	@Value("${mail.smtp.port}")
	private Integer smtpPort;
	@Value("${mail.smtp.sslPort}")
	private String sslSmtpPort;
	@Value("${mail.smtp.enableSSL}")
	private Boolean enableSSL;

	private MailServiceImpl() {
		super();
		
	}

	private Configuration configuration;
	@Autowired
	public void setConfiguration(FreeMarkerConfigurer freeMarkerConfigurer) {
		this.configuration = freeMarkerConfigurer.getConfiguration();
	}

	private void addAttachments(List<Pair<String, File>> attachments, MultiPartEmail email) {
		try {
			for (Pair<String, File> attachment : attachments) {
				MimeBodyPart attachmentPart = new MimeBodyPart();
				FileDataSource fileDataSource = new FileDataSource(attachment.getValue()) {
					@Override
					public String getContentType() {
						return "application/octet-stream";
					}
				};
				attachmentPart.setDataHandler(new DataHandler(fileDataSource));
				attachmentPart.setFileName(attachment.getKey());
				MimeMultipart multipart = new MimeMultipart();
				multipart.addBodyPart(attachmentPart);
				email.addPart(multipart);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private HtmlEmail createEmail(MailContent mail) throws EmailException {
		HtmlEmail email = new HtmlEmail();
		email.setHostName(smtpServer);
		email.setSmtpPort(smtpPort);
		email.setAuthentication(mailUser, mailPwd);
		if (enableSSL) {
			email.setSSL(true);
			email.setSslSmtpPort(sslSmtpPort);
		}
		if (mail.getFrom() == null)
			email.setFrom(mailAddress, MAIL_FROM_USERNAME, CHARSET);
		else {
			email.setFrom(mail.getFrom().getAddress(), mail.getFrom().getPersonal(), CHARSET);
		}
		// for content and title
		email.setCharset(CHARSET);
		return email;
	}

	@Override
	public void send(MailContent mail) {
		logger.info("send email: title:{}", mail.getSubject());
		try {
			HtmlEmail htmlEmail = createEmail(mail);
			BizAssert.hasText(mail.getSubject(), "error.mail.title.blank");
			htmlEmail.setSubject(mail.getSubject());
			String content = getContent(mail);
			BizAssert.hasText(content, "error.mail.body.blank");
			if (mail.isHtmlMail()) {
				htmlEmail.setHtmlMsg(content);
			} else {
				htmlEmail.setMsg(content);
			}
			htmlEmail.setTo(mail.getTo());
			if (CollectionUtils.isNotEmpty(mail.getCc())) {
				htmlEmail.setCc(mail.getCc());
			}
			if (CollectionUtils.isNotEmpty(mail.getBcc())) {
				htmlEmail.setBcc(mail.getBcc());
			}

			if (mail.getAttachments() != null) {
				addAttachments(mail.getAttachments(), htmlEmail);
			}

			htmlEmail.send();
		} catch (EmailException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private String getContent(MailContent mail) {
		String content = null;
		if (StringUtils.isNotEmpty(mail.getBody())) {
			content = mail.getBody();
		} else if (StringUtils.isNotEmpty(mail.getBodyTemplate())) {
			content = fillTemplate(mail.getBodyTemplate(), mail.getBodyTplData());
		}
		return content;
	}

	private String fillTemplate(String mailTemplate, Map fillData) {
		try {
			StringWriter out = new StringWriter();
			Template temp = configuration.getTemplate(mailTemplate);
			temp.process(fillData, out);
			return out.toString();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
