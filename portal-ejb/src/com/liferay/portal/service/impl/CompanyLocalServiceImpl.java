/**
 * Copyright (c) 2000-2006 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.service.impl;

import com.liferay.portal.AccountNameException;
import com.liferay.portal.CompanyHomeURLException;
import com.liferay.portal.CompanyPortalURLException;
import com.liferay.portal.NoSuchAccountException;
import com.liferay.portal.NoSuchCompanyException;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.lucene.LuceneFields;
import com.liferay.portal.lucene.LuceneUtil;
import com.liferay.portal.model.Account;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.model.impl.CompanyImpl;
import com.liferay.portal.model.impl.ContactImpl;
import com.liferay.portal.model.impl.RoleImpl;
import com.liferay.portal.model.impl.UserImpl;
import com.liferay.portal.service.CompanyLocalService;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.persistence.AccountUtil;
import com.liferay.portal.service.persistence.CompanyUtil;
import com.liferay.portal.service.persistence.ContactUtil;
import com.liferay.portal.service.persistence.UserUtil;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.portal.util.PropsUtil;
import com.liferay.util.Encryptor;
import com.liferay.util.EncryptorException;
import com.liferay.util.FileUtil;
import com.liferay.util.GetterUtil;
import com.liferay.util.ImageUtil;
import com.liferay.util.Validator;
import com.liferay.util.lucene.HitsImpl;

import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Searcher;

/**
 * <a href="CompanyLocalServiceImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Brian Wing Shun Chan
 *
 */
public class CompanyLocalServiceImpl implements CompanyLocalService {

	public void checkCompany(String companyId)
		throws PortalException, SystemException {

		// Company

		String companyMx = companyId;
		Date now = new Date();

		Company company = null;

		try {
			company = CompanyUtil.findByPrimaryKey(companyId);
		}
		catch (NoSuchCompanyException nsce) {
			String portalURL = "localhost";
			String homeURL = "localhost";
			String name = companyId;
			String legalName = null;
			String legalId = null;
			String legalType = null;
			String sicCode = null;
			String tickerSymbol = null;
			String industry = null;
			String type = null;
			String size = null;

			company = CompanyUtil.create(companyId);

			try {
				company.setKeyObj(Encryptor.generateKey());
			}
			catch (EncryptorException ee) {
				throw new SystemException(ee);
			}

			company.setPortalURL(portalURL);
			company.setHomeURL(homeURL);
			company.setMx(companyMx);

			CompanyUtil.update(company);

			updateCompany(
				companyId, portalURL, homeURL, companyMx, name, legalName,
				legalId, legalType, sicCode, tickerSymbol, industry, type,
				size);

			// Demo settings

			if (companyId.equals("liferay.net")) {
				company = CompanyUtil.findByPrimaryKey(companyId);

				company.setPortalURL("demo.liferay.net");
				company.setHomeURL("demo.liferay.net");

				CompanyUtil.update(company);

				updateSecurity(
					companyId, CompanyImpl.AUTH_TYPE_EA, true, true, true);

				PortletPreferences prefs =
					PrefsPropsUtil.getPreferences(companyId);

				try {
					prefs.setValue(
						PropsUtil.ADMIN_EMAIL_FROM_NAME, "Liferay Demo");
					prefs.setValue(
						PropsUtil.ADMIN_EMAIL_FROM_ADDRESS, "test@liferay.net");

					prefs.store();
				}
				catch (IOException ioe) {
					throw new SystemException(ioe);
				}
				catch (PortletException pe) {
					throw new SystemException(pe);
				}
			}
		}

		// Key

		checkCompanyKey(companyId);

		// Default user

		User defaultUser = null;

		try {
			defaultUser = UserLocalServiceUtil.getDefaultUser(companyId);

			if (!defaultUser.isAgreedToTermsOfUse()) {
				defaultUser.setAgreedToTermsOfUse(true);

				UserUtil.update(defaultUser);
			}
		}
		catch (NoSuchUserException nsue) {
			defaultUser = UserUtil.create(UserImpl.getDefaultUserId(companyId));

			defaultUser.setCompanyId(UserImpl.DEFAULT);
			defaultUser.setCreateDate(now);
			defaultUser.setModifiedDate(now);
			defaultUser.setPassword("password");
			defaultUser.setEmailAddress(UserImpl.DEFAULT + "@" + companyMx);
			defaultUser.setLanguageId(null);
			defaultUser.setTimeZoneId(null);
			defaultUser.setGreeting("Welcome!");
			defaultUser.setResolution(
				PropsUtil.get(PropsUtil.DEFAULT_GUEST_LAYOUT_RESOLUTION));
			defaultUser.setLoginDate(now);
			defaultUser.setFailedLoginAttempts(0);
			defaultUser.setAgreedToTermsOfUse(true);
			defaultUser.setActive(true);

			UserUtil.update(defaultUser);

			// Contact

			Contact defaultContact = ContactUtil.create(
				defaultUser.getUserId());

			defaultContact.setCompanyId(defaultUser.getCompanyId());
			defaultContact.setUserId(defaultUser.getUserId());
			defaultContact.setUserName(StringPool.BLANK);
			defaultContact.setCreateDate(now);
			defaultContact.setModifiedDate(now);
			defaultContact.setAccountId(defaultUser.getCompanyId());
			defaultContact.setParentContactId(
				ContactImpl.DEFAULT_PARENT_CONTACT_ID);
			defaultContact.setFirstName(StringPool.BLANK);
			defaultContact.setMiddleName(StringPool.BLANK);
			defaultContact.setLastName(StringPool.BLANK);
			defaultContact.setMale(true);
			defaultContact.setBirthday(now);

			ContactUtil.update(defaultContact);
		}

		// System groups

		GroupLocalServiceUtil.checkSystemGroups(companyId);

		// System roles

		RoleLocalServiceUtil.checkSystemRoles(companyId);

		// Default admin

		if (UserUtil.countByCompanyId(companyId) == 0) {
			String creatorUserId = null;
			boolean autoUserId = true;
			String userId = StringPool.BLANK;
			boolean autoPassword = false;
			String password1 = PropsUtil.get(PropsUtil.DEFAULT_ADMIN_PASSWORD);
			String password2 = password1;
			boolean passwordReset = false;
			String emailAddress =
				PropsUtil.get(
					PropsUtil.DEFAULT_ADMIN_EMAIL_ADDRESS_PREFIX) +
				"@" + companyMx;
			Locale locale = defaultUser.getLocale();
			String firstName = PropsUtil.get(
				PropsUtil.DEFAULT_ADMIN_FIRST_NAME);
			String middleName = PropsUtil.get(
				PropsUtil.DEFAULT_ADMIN_MIDDLE_NAME);
			String lastName = PropsUtil.get(PropsUtil.DEFAULT_ADMIN_LAST_NAME);
			String nickName = StringPool.BLANK;
			int prefixId = 0;
			int suffixId = 0;
			boolean male = true;
			int birthdayMonth = Calendar.JANUARY;
			int birthdayDay = 1;
			int birthdayYear = 1970;
			String jobTitle = StringPool.BLANK;
			String organizationId = null;
			String locationId = null;

			User user = UserLocalServiceUtil.addUser(
				creatorUserId, companyId, autoUserId, userId, autoPassword,
				password1, password2, passwordReset, emailAddress, locale,
				firstName, middleName, lastName, nickName, prefixId, suffixId,
				male, birthdayMonth, birthdayDay, birthdayYear, jobTitle,
				organizationId, locationId, false);

			Role adminRole = RoleLocalServiceUtil.getRole(
				companyId, RoleImpl.ADMINISTRATOR);

			Role powerUserRole = RoleLocalServiceUtil.getRole(
				companyId, RoleImpl.POWER_USER);

			String[] roleIds = new String[] {
				adminRole.getRoleId(), powerUserRole.getRoleId()
			};

			RoleLocalServiceUtil.setUserRoles(user.getUserId(), roleIds);
		}
	}

	public void checkCompanyKey(String companyId)
		throws PortalException, SystemException {

		Company company = CompanyUtil.findByPrimaryKey(companyId);

		if (company.getKeyObj() == null) {
			try {
				company.setKeyObj(Encryptor.generateKey());
			}
			catch (EncryptorException ee) {
				throw new SystemException(ee);
			}
		}

		CompanyUtil.update(company);
	}

	public List getCompanies() throws SystemException {
		return CompanyUtil.findAll();
	}

	public Company getCompany(String companyId)
		throws PortalException, SystemException {

		return CompanyUtil.findByPrimaryKey(companyId);
	}

	public Hits search(String companyId, String keywords)
		throws SystemException {

		return search(companyId, null, 0, null, keywords);
	}

	public Hits search(
			String companyId, String portletId, long groupId, String type,
			String keywords)
		throws SystemException {

		try {
			HitsImpl hits = new HitsImpl();

			if (Validator.isNull(keywords)) {
				return hits;
			}

			BooleanQuery contextQuery = new BooleanQuery();

			LuceneUtil.addRequiredTerm(
				contextQuery, LuceneFields.COMPANY_ID, companyId);

			if (Validator.isNotNull(portletId)) {
				LuceneUtil.addRequiredTerm(
					contextQuery, LuceneFields.PORTLET_ID, portletId);
			}

			if (groupId > 0) {
				LuceneUtil.addRequiredTerm(
					contextQuery, LuceneFields.GROUP_ID, groupId);
			}

			if (Validator.isNotNull(type)) {
				LuceneUtil.addRequiredTerm(
					contextQuery, LuceneFields.TYPE, type);
			}

            BooleanQuery searchQuery = new BooleanQuery();

			LuceneUtil.addTerm(searchQuery, LuceneFields.TITLE, keywords);
			LuceneUtil.addTerm(searchQuery, LuceneFields.CONTENT, keywords);

			BooleanQuery fullQuery = new BooleanQuery();

			fullQuery.add(contextQuery, BooleanClause.Occur.MUST);
			fullQuery.add(searchQuery, BooleanClause.Occur.MUST);

			Searcher searcher = LuceneUtil.getSearcher(companyId);

			hits.recordHits(searcher.search(fullQuery));

			return hits;
		}
		catch (BooleanQuery.TooManyClauses tmc) {
			if (_log.isErrorEnabled()) {
				_log.error("Keywords " + keywords);
			}

			throw new SystemException(tmc);
		}
		catch (IOException ioe) {
			throw new SystemException(ioe);
		}
		catch (ParseException pe) {
			_log.error("Parsing keywords " + keywords, pe);

			return new HitsImpl();
		}
	}

	public Company updateCompany(
			String companyId, String portalURL, String homeURL, String mx,
			String name, String legalName, String legalId, String legalType,
			String sicCode, String tickerSymbol, String industry, String type,
			String size)
		throws PortalException, SystemException {

		// Company

		Date now = new Date();

		validate(portalURL, homeURL, name);

		Company company = CompanyUtil.findByPrimaryKey(companyId);

		company.setPortalURL(portalURL);
		company.setHomeURL(homeURL);

		if (GetterUtil.getBoolean(PropsUtil.get(PropsUtil.MAIL_MX_UPDATE))) {
			company.setMx(mx);
		}

		CompanyUtil.update(company);

		// Account

		String accountId = companyId;

		Account account = null;

		try {
			account = AccountUtil.findByPrimaryKey(accountId);
		}
		catch (NoSuchAccountException nsae) {
			account = AccountUtil.create(accountId);

			account.setCreateDate(now);
			account.setCompanyId(UserImpl.DEFAULT);
			account.setUserId(UserImpl.getDefaultUserId(companyId));
			account.setUserName(StringPool.BLANK);
		}

		account.setModifiedDate(now);
		account.setName(name);
		account.setLegalName(legalName);
		account.setLegalId(legalId);
		account.setLegalType(legalType);
		account.setSicCode(sicCode);
		account.setTickerSymbol(tickerSymbol);
		account.setIndustry(industry);
		account.setType(type);
		account.setSize(size);

		AccountUtil.update(account);

		return company;
	}

	public void updateDisplay(
			String companyId, String languageId, String timeZoneId,
			String resolution)
		throws PortalException, SystemException {

		User user = UserLocalServiceUtil.getDefaultUser(companyId);

		user.setLanguageId(languageId);
		user.setTimeZoneId(timeZoneId);
		user.setResolution(resolution);

		UserUtil.update(user);
	}

	public void updateLogo(String companyId, File file)
		throws PortalException, SystemException {

		try {
			ImageLocalUtil.put(companyId, FileUtil.getBytes(file));

			BufferedImage thumbnail = ImageUtil.scale(ImageIO.read(file), .6);

			// PNG

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			ImageIO.write(thumbnail, "png", baos);

			ImageLocalUtil.put(companyId + ".png", baos.toByteArray());

			// WBMP

			baos = new ByteArrayOutputStream();

			ImageUtil.encodeWBMP(thumbnail, baos);

			ImageLocalUtil.put(companyId + ".wbmp", baos.toByteArray());
		}
		catch (InterruptedException ie) {
			throw new SystemException(ie);
		}
		catch (IOException ioe) {
			throw new SystemException(ioe);
		}
	}

	public void updateSecurity(
			String companyId, String authType, boolean autoLogin,
			boolean sendPassword, boolean strangers)
		throws PortalException, SystemException {

		PortletPreferences prefs = PrefsPropsUtil.getPreferences(companyId);

		try {
			prefs.setValue(PropsUtil.COMPANY_SECURITY_AUTH_TYPE, authType);
			prefs.setValue(
				PropsUtil.COMPANY_SECURITY_AUTO_LOGIN,
				Boolean.toString(autoLogin));
			prefs.setValue(
				PropsUtil.COMPANY_SECURITY_SEND_PASSWORD,
				Boolean.toString(sendPassword));
			prefs.setValue(
				PropsUtil.COMPANY_SECURITY_STRANGERS,
				Boolean.toString(strangers));

			prefs.store();
		}
		catch (IOException ioe) {
			throw new SystemException(ioe);
		}
		catch (PortletException pe) {
			throw new SystemException(pe);
		}
	}

	protected void validate(String portalURL, String homeURL, String name)
		throws PortalException {

		if (Validator.isNull(portalURL)) {
			throw new CompanyPortalURLException();
		}
		else if (Validator.isNull(homeURL)) {
			throw new CompanyHomeURLException();
		}
		else if (Validator.isNull(name)) {
			throw new AccountNameException();
		}
	}

	private static Log _log = LogFactory.getLog(CompanyLocalServiceImpl.class);

}
