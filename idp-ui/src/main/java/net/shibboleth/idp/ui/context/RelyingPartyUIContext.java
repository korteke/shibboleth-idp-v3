/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.idp.ui.context;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.messaging.context.BaseContext;
import org.opensaml.saml.ext.saml2mdui.Description;
import org.opensaml.saml.ext.saml2mdui.DisplayName;
import org.opensaml.saml.ext.saml2mdui.InformationURL;
import org.opensaml.saml.ext.saml2mdui.Logo;
import org.opensaml.saml.ext.saml2mdui.PrivacyStatementURL;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml.saml2.metadata.OrganizationName;
import org.opensaml.saml.saml2.metadata.OrganizationURL;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.ServiceDescription;
import org.opensaml.saml.saml2.metadata.ServiceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * The context which carries the user interface information.
 */
public class RelyingPartyUIContext extends BaseContext {

    /** The log. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(RelyingPartyUIContext.class);

    /** The appropriate {@link EntityDescriptor}. */
    @Nullable private EntityDescriptor rpEntityDescriptor;

    /** The appropriate {@link SPSSODescriptor}. */
    @Nullable private SPSSODescriptor rpSPSSODescriptor;

    /** The appropriate {@link AttributeConsumingService}. */
    @Nullable private AttributeConsumingService rpAttributeConsumingService;

    /** The appropriate {@link UIInfo}. */
    @Nullable private UIInfo rpUIInfo;

    /** The languages that this browser wants to know about. */
    @Nonnull @NonnullElements private List<String> browserLanguages;
    
    /** The languages that this the Operator want to fall back to. */
    @Nullable private List<String> fallbackLanguages;

    /** The languages that this the bean needs to look at. */
    @Nonnull @NonnullElements @Unmodifiable private List<String> usableLanguages;
    
    /** Constructor. */
    public RelyingPartyUIContext() {
        browserLanguages = Collections.emptyList();
        usableLanguages = Collections.emptyList();
    }

    /**
     * Get the {@link EntityDescriptor}.
     * 
     * @return Returns the entity.
     */
    @Nullable protected EntityDescriptor getRPEntityDescriptor() {
        return rpEntityDescriptor;
    }

    /**
     * Set the {@link EntityDescriptor}.
     * 
     * @param what what to set
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyUIContext setRPEntityDescriptor(@Nullable final EntityDescriptor what) {
        rpEntityDescriptor = what;
        return this;
    }

    /**
     * Get the {@link SPSSODescriptor}.
     * 
     * @return Returns the SPSSODescriptor.
     */
    @Nullable protected SPSSODescriptor getRPSPSSODescriptor() {
        return rpSPSSODescriptor;
    }

    /**
     * Set the {@link SPSSODescriptor}.
     * 
     * @param what what to set
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyUIContext setRPSPSSODescriptor(@Nullable final SPSSODescriptor what) {
        rpSPSSODescriptor = what;
        return this;
    }

    /**
     * Get the {@link AttributeConsumingService} for the request.
     * 
     * @return Returns the SPSSODescriptor.
     */
    @Nullable protected AttributeConsumingService getRPAttributeConsumingService() {
        return rpAttributeConsumingService;
    }

    /**
     * Get the RP {@link UIInfo} associated with the request.
     * 
     * @return the value or null if there is none.
     */
    @Nullable protected UIInfo getRPUInfo() {
        return rpUIInfo;
    }

    /**
     * Set the RP {@link UIInfo} associated with the request.
     * 
     * @param what the value to set
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyUIContext setRPUInfo(@Nullable final UIInfo what) {
        rpUIInfo = what;
        return this;
    }

    /**
     * Set the {@link AttributeConsumingService} for the request.
     * 
     * @param what what to set
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyUIContext setRPAttributeConsumingService(
            @Nullable final AttributeConsumingService what) {
        rpAttributeConsumingService = what;
        return this;
    }

    /**
     * Set the browser languages.
     * 
     * @param languages the languages to set
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyUIContext setBrowserLanguages(@Nonnull @NonnullElements final List<String> languages) {
        browserLanguages = Constraint.isNotNull(languages, "Language List cannot be null");
        makeNewusableList();
        return this;
    }

    /**
     * Get the browser languages.
     * 
     * @return the languages.
     */
    @Nonnull @NonnullElements protected List<String> getBrowserLanguages() {
        return browserLanguages;
    }

    /**
     * Set the fallback languages.
     * 
     * @param languages the languages to set
     * 
     * @return this context
     */
    @Nonnull public RelyingPartyUIContext setFallbackLanguages(@Nullable final List<String> languages) {
        fallbackLanguages = languages;
        makeNewusableList();        
        return this;
    }

    /**
     * Get the fallback languages.
     * 
     * @return the languages.
     */
    @Nonnull @NonnullElements protected List<String> getFallbackLanguages() {
        return fallbackLanguages;
    }

    /**
     * Construct the usableLanguages from the {@link #browserLanguages} and the {@link #fallbackLanguages}. 
     */
    protected void makeNewusableList() {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        if (null != browserLanguages) {
            builder.addAll(browserLanguages);
        }
        if (null != fallbackLanguages) {
            builder.addAll(fallbackLanguages);
        }
        usableLanguages = builder.build();
    }

    /**
     * Get the all the languages.
     * 
     * @return the languages.
     */
    @Nonnull @NonnullElements @Unmodifiable protected List<String> getUsableLanguages() {
        return usableLanguages;
    }

    /**
     * Check to see whether a supplied URL is acceptable, returning the default if it isn't.
     * 
     * @param url the url to look at
     * @param acceptableSchemes the schemes to test against
     * @return the input or null as appropriate.
     */
    @Nullable private String policeURL(@Nullable final String url,
            @Nonnull @NotEmpty final List<String> acceptableSchemes) {
        if (null == url) {
            log.trace("Empty Value - returning null");
            return null;
        }

        try {
            final String scheme = new URI(url).getScheme();

            for (final String acceptableScheme : acceptableSchemes) {
                if (acceptableScheme.equals(scheme)) {
                    log.debug("Acceptable Scheme '{}', returning value '{}'", acceptableScheme, url);
                    return url;
                }
            }

            log.warn("The logo URL '{}' contained an invalid scheme (expected '{}'), returning null", url,
                    acceptableSchemes);
            return null;
        } catch (URISyntaxException e) {
            log.warn("The logo URL '{}' contained was not a URL, returning null", url);
            return null;
        }
    }

    /**
     * Police a url found for a logo.
     * 
     * @param url the url to look at
     * @return the input or the default as appropriate
     */
    protected String policeURLLogo(@Nullable final String url) {
        return policeURL(url, Arrays.asList("http", "https", "data"));
    }

    /**
     * Police a url found for non logo data.
     * 
     * @param url the url to look at
     * @return the input or the default as appropriate
     */
    protected String policeURLNonLogo(@Nullable final String url) {
        return policeURL(url, Arrays.asList("http", "https", "mailto"));
    }

    /**
     * look at &lt;UIinfo&gt;; if there and if so look for appropriate name.
     * 
     * @param lang - which language to look up
     * @return null or an appropriate name
     */
    @Nullable protected String getNameFromUIInfo(final String lang) {

        if (getRPUInfo() != null) {
            for (final DisplayName name : getRPUInfo().getDisplayNames()) {
                log.trace("Found name in UIInfo, language '{}'", name.getXMLLang());
                if (name.getXMLLang() != null && name.getXMLLang().equals(lang)) {
                    log.debug("Returning name from UIInfo '{}'", name.getValue());
                    return name.getValue();
                }
            }
        }
        log.trace("No name in UIINFO for '{}'", lang);
        return null;
    }

    /**
     * Look for an &lt;AttributeConsumeService&gt; and if its there look for an appropriate name.
     * 
     * @param lang - which language to look up
     * @return null or an appropriate name
     */
    @Nullable protected String getNameFromAttributeConsumingService(final String lang) {

        if (null == getRPAttributeConsumingService()) {
            return null;
        }

        for (ServiceName name : getRPAttributeConsumingService().getNames()) {
            log.trace("Found name in AttributeConsumingService, language '{}'", name.getXMLLang());
            if (name.getXMLLang() != null && name.getXMLLang().equals(lang)) {
                log.debug("Returning name from AttributeConsumingService '{}", name.getValue());
                return name.getValue();
            }
        }
        log.trace("No name found in AttributeConsumingService for '{}'", lang);
        return null;
    }

    /**
     * If the entityId can look like a host return that, otherwise the entityId in full.
     * 
     * @return either the host or the entityId.
     */
    @Nullable protected String getNameFromEntityId() {

        if (null == getRPEntityDescriptor()) {
            log.trace("No relying party, no Name");
            return null;
        }
        final String spName = getRPEntityDescriptor().getEntityID();

        try {
            final URI entityId = new URI(spName);
            final String scheme = entityId.getScheme();

            if ("http".equals(scheme) || "https".equals(scheme)) {
                log.debug("Found matching scheme, returning name of '{}'", entityId.getHost());
                return entityId.getHost();
            }
            log.debug("Not a usual scheme, returning name of '{}'", spName);

            return spName;
        } catch (final URISyntaxException e) {
            log.debug("Not a URI, returning name of '{}'", spName);
            return spName;
        }
    }

    /**
     * look at &lt;UIInfo&gt; if there and if so look for appropriate description.
     * 
     * @param lang - which language to look up
     * @return null or an appropriate description
     */
    @Nullable protected String getDescriptionFromUIInfo(final String lang) {
        if (getRPUInfo() == null || getRPUInfo().getDescriptions() == null) {
            log.trace("No UIInfo");
            return null;
        }
        for (final Description desc : getRPUInfo().getDescriptions()) {
            log.trace("Found description in UIInfo, language '{}'", desc.getXMLLang());
            if (desc.getXMLLang() != null && desc.getXMLLang().equals(lang)) {
                log.trace("Returning description from UIInfo '{}'", desc.getValue());
                return desc.getValue();
            }
        }
        log.debug("No matching description in UIInfo");
        return null;
    }

    /**
     * look for an &ltAttributeConsumeService&gt and if its there look for an appropriate description.
     * 
     * @param lang - which language to look up
     * @return null or an appropriate description
     */
    @Nullable protected String getDescriptionFromAttributeConsumingService(final String lang) {
        if (getRPAttributeConsumingService() == null) {
            log.trace("No ACS found");
            return null;
        }
        for (final ServiceDescription desc : getRPAttributeConsumingService().getDescriptions()) {
            log.trace("Found name in AttributeConsumingService, language=" + desc.getXMLLang());
            if (desc.getXMLLang() != null && desc.getXMLLang().equals(lang)) {
                log.debug("Returning name from AttributeConsumingService " + desc.getValue());
                return desc.getValue();
            }
        }
        log.trace("No description in AttributeConsumingService");

        return null;
    }

    /**
     * Get the {@link Organization} from the {@link SPSSODescriptor}, failing that the {@link EntityDescriptor}.
     * 
     * @return the {@link Organization} for the relying party.
     */
    @Nullable protected Organization getOrganization() {
        if (null != getRPSPSSODescriptor() && null != getRPSPSSODescriptor().getOrganization()) {
            return getRPSPSSODescriptor().getOrganization();
        }
        if (null != getRPEntityDescriptor() && null != getRPEntityDescriptor().getOrganization()) {
            return getRPEntityDescriptor().getOrganization();
        }
        return null;
    }

    /**
     * Parser for the contact type.
     * 
     * @param type in value
     * @return the enumeration type. suitably defaulted.
     */
    protected ContactPersonTypeEnumeration getContactType(@Nullable final String type) {
        final String value = StringSupport.trimOrNull(type);
        if (null == value) {
            log.warn("no parameter provided to contactType");
            return ContactPersonTypeEnumeration.SUPPORT;
        }
        if (type.equals(ContactPersonTypeEnumeration.ADMINISTRATIVE.toString())) {
            return ContactPersonTypeEnumeration.ADMINISTRATIVE;
        } else if (type.equals(ContactPersonTypeEnumeration.BILLING.toString())) {
            return ContactPersonTypeEnumeration.BILLING;
        } else if (type.equals(ContactPersonTypeEnumeration.OTHER.toString())) {
            return ContactPersonTypeEnumeration.OTHER;
        } else if (type.equals(ContactPersonTypeEnumeration.SUPPORT.toString())) {
            return ContactPersonTypeEnumeration.SUPPORT;
        } else if (type.equals(ContactPersonTypeEnumeration.TECHNICAL.toString())) {
            return ContactPersonTypeEnumeration.TECHNICAL;
        } else {
            log.warn("parameter provided to contactType: " + type + " is invalid");
            return ContactPersonTypeEnumeration.SUPPORT;
        }
    }

    /**
     * Lookup the specified type of Contact in the RP metadata.
     * 
     * @param contactType what type to look up.
     * @return the {@link ContactPerson} or null.
     */
    @Nullable public ContactPerson getContactPerson(ContactPersonTypeEnumeration contactType) {
        if (null == getRPEntityDescriptor()) {
            return null;
        }
        final List<ContactPerson> contacts = getRPEntityDescriptor().getContactPersons();
        if (null == contacts || contacts.isEmpty()) {
            log.trace("No Contacts found at all");
            return null;
        }
        for (final ContactPerson contact : contacts) {
            if (contactType == contact.getType()) {
                return contact;
            }
        }
        log.trace("No matching Contacts found at all");
        return null;
    }

    /**
     * Get the service name.
     * 
     * @return the name or null if there wasn't one 
     */
    @Nullable public String getServiceName() {

        if (getRPEntityDescriptor() == null) {
            log.debug("No relying party, no name, returning null");
            return null;
        }

        for (final String lang : getUsableLanguages()) {
            String result;
            result = getNameFromUIInfo(lang);
            if (result != null) {
                return result;
            }

            result = getNameFromAttributeConsumingService(lang);
            if (result != null) {
                return result;
            }
        }
        // failing that just look at the entity name
        return getNameFromEntityId();
    }

    /**
     * Get the service Description.
     * 
     * @return the description or null if there wasn't one 
     */
    @Nullable public String getServiceDescription() {

        for (final String lang : getUsableLanguages()) {
            String value = getDescriptionFromUIInfo(lang);
            if (null != value) {
                return value;
            }
            value = getDescriptionFromAttributeConsumingService(lang);
            if (null != value) {
                return value;
            }
        }
        log.debug("No description matching the languages found, returning null");
        return null;
    }

    /**
     * Look for the &lt;OrganizationDisplayName&gt;.
     * 
     * @return An appropriate string or null
     */
    @Nullable public String getOrganizationDisplayName() {
        final Organization org = getOrganization();
        if (null == org || null == org.getDisplayNames() || org.getDisplayNames().isEmpty()) {
            log.debug("No Organization, OrganizationDisplayName or names, returning null");
            return null;
        }
        for (final String lang : getUsableLanguages()) {
            for (final OrganizationDisplayName name : org.getDisplayNames()) {
                log.trace("Found OrganizationDisplayName in Organization, language={}", name.getXMLLang());

                if (name.getXMLLang() != null && name.getXMLLang().equals(lang)) {
                    log.debug("Returning OrganizationDisplayName from Organization, {}", name.getValue());
                    return name.getValue();
                }
            }
        }
        log.debug("No relevant OrganizationDisplayName in Organization, returning null");
        return null;
    }

    /**
     * Look for the &lt;OrganizationName&gt;.
     * 
     * @return An appropriate string or null
     */
    @Nullable public String getOrganizationName() {
        final Organization org = getOrganization();
        if (null == org || null == org.getOrganizationNames() || org.getOrganizationNames().isEmpty()) {
            log.debug("No Organization, OrganizationName or names, returning null");
            return null;
        }
        for (final String lang : getUsableLanguages()) {
            for (final OrganizationName name : org.getOrganizationNames()) {
                log.trace("Found OrganizationName in Organization, language={}", name.getXMLLang());

                if (name.getXMLLang() != null && name.getXMLLang().equals(lang)) {
                    log.debug("Returning OrganizationName from Organization, {}", name.getValue());
                    return name.getValue();
                }
            }
        }
        log.debug("No relevant OrganizationName in Organization, returning null");
        return null;
    }

    /**
     * * Look for the &lt;OrganizationURL&gt;.
     * 
     * @return An appropriate string or the null
     */
    public String getOrganizationURL() {
        final Organization org = getOrganization();
        if (null == org || null == org.getURLs() || org.getURLs().isEmpty()) {
            log.debug("No Organization, OrganizationURL or urls, returning null");
            return null;
        }
        for (final String lang : getUsableLanguages()) {
            for (final OrganizationURL url : org.getURLs()) {
                log.trace("Found OrganizationURL in Organization, language={}", url.getXMLLang());

                if (url.getXMLLang() != null && url.getXMLLang().equals(lang)) {
                    log.debug("Returning OrganizationURL from Organization, {}", url.getValue());
                    return policeURLNonLogo(url.getValue());
                }
            }
        }
        log.debug("No relevant OrganizationURL in Organization, returning null");
        return null;
    }

    /**
     * look for the &lt;ContactPerson&gt; and within that the SurName.
     * 
     * @param contactType the type of contact to look for
     * @return An appropriate string or null
     */
    @Nullable public String getContactSurName(@Nullable String contactType) {

        final ContactPerson contact = getContactPerson(getContactType(contactType));
        if (null == contact || null == contact.getSurName()) {
            return null;
        }
        return contact.getSurName().getName();
    }

    /**
     * look for the &lt;ContactPerson&gt; and within that the GivenName.
     * 
     * @param contactType the type of contact to look for
     * @return An appropriate string or null
     */
    @Nullable public String getContactGivenName(@Nullable String contactType) {

        final ContactPerson contact = getContactPerson(getContactType(contactType));
        if (null == contact || null == contact.getGivenName()) {
            return null;
        }
        return contact.getGivenName().getName();
    }

    /**
     * look for the &lt;ContactPerson&gt; and within that the Email.
     * 
     * @param contactType the type of contact to look for
     * @return An appropriate string or null
     */
    @Nullable public String getContactEmail(@Nullable String contactType) {

        final ContactPerson contact = getContactPerson(getContactType(contactType));
        if (null == contact || null == contact.getEmailAddresses() || contact.getEmailAddresses().isEmpty()) {
            return null;
        }
        return policeURLNonLogo(contact.getEmailAddresses().get(0).getAddress());
    }

    /**
     * Get the &lt;mdui:InformationURL&gt;.
     * 
     * @return the value or the default value
     */
    @Nullable public String getInformationURL() {

        if (null == getRPUInfo() || null == rpUIInfo.getInformationURLs() || rpUIInfo.getInformationURLs().isEmpty()) {
            log.debug("No UIInfo or InformationURLs returning null");
            return null;
        }
        for (final String lang : getUsableLanguages()) {
            for (final InformationURL url : rpUIInfo.getInformationURLs()) {
                log.trace("Found InformationURL, language={}", url.getXMLLang());

                if (url.getXMLLang() != null && url.getXMLLang().equals(lang)) {
                    log.debug("Returning InformationURL, {}", url.getValue());
                    return policeURLNonLogo(url.getValue());
                }
            }
        }
        log.debug("No relevant InformationURL with language match, returning null");
        return null;
    }

    /**
     * Get the &lt;mdui:PrivacyStatementURL&gt;.
     * 
     * @return the value or null
     */
    @Nullable public String getPrivacyStatementURL() {
        if (null == getRPUInfo() || null == rpUIInfo.getPrivacyStatementURLs()
                || rpUIInfo.getPrivacyStatementURLs().isEmpty()) {
            log.debug("No UIInfo or PrivacyStatementURLs returning null");
            return null;
        }
        for (final String lang : getUsableLanguages()) {
            for (final PrivacyStatementURL url : rpUIInfo.getPrivacyStatementURLs()) {
                log.trace("Found PrivacyStatementURL, language={}", url.getXMLLang());

                if (url.getXMLLang() != null && url.getXMLLang().equals(lang)) {
                    log.debug("Returning PrivacyStatementURL, {}", url.getValue());
                    return policeURLNonLogo(url.getValue());
                }
            }
        }
        log.debug("No relevant PrivacyStatementURLs with language match, returning null");
        return null;
    }

    /**
     * Does the logo fit the supplied parameters?
     * 
     * @param logo the logo
     * @param minWidth min Width
     * @param minHeight min Height
     * @param maxWidth max Width
     * @param maxHeight max Height
     * @return whether it fits
     */
    private boolean logoFits(Logo logo, int minWidth, int minHeight, int maxWidth, int maxHeight) {
        return logo.getHeight() <= maxHeight && logo.getHeight() >= minHeight && logo.getWidth() <= maxWidth
                && logo.getWidth() >= minWidth;
    }

    /**
     * Get the Logo of the given language which fits the size.
     * 
     * @param lang the language
     * @param minWidth the minimum width to allow.
     * @param minHeight the minimum height to allow.
     * @param maxWidth the maximum width to allow.
     * @param maxHeight the maximum height to allow.
     * @return an appropriate logo URL or null.
     */
    @Nullable private String getLogoByLanguage(@Nonnull String lang, int minWidth, int minHeight, int maxWidth,
            int maxHeight) {
        for (final Logo logo : rpUIInfo.getLogos()) {
            log.trace("Found logo in UIInfo, '{}' ({} x {})", logo.getXMLLang(), logo.getWidth(), logo.getHeight());
            if (logo.getXMLLang() == null || !logo.getXMLLang().equals(lang)) {
                log.trace("Language mismatch against '{}'");
                continue;
            }
            if (!logoFits(logo, minWidth, minHeight, maxWidth, maxHeight)) {
                log.trace("Size mismatch");
                continue;
            }
            log.debug("Returning logo from UIInfo, '{}' ({} x {}) : {}", logo.getXMLLang(), logo.getWidth(),
                    logo.getHeight(), logo.getURL());
            return logo.getURL();
        }
        return null;
    }

    /**
     * Get a Logo without a language which fits the size.
     * 
     * @param minWidth the minimum width to allow.
     * @param minHeight the minimum height to allow.
     * @param maxWidth the maximum width to allow.
     * @param maxHeight the maximum height to allow.
     * @return an appropriate logo URL or null.
     */
    @Nullable private String getLogoNoLanguage(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        for (final Logo logo : rpUIInfo.getLogos()) {
            if (null != logo.getXMLLang()) {
                continue;
            }
            log.trace("Found logo in UIInfo, ({} x {})", logo.getWidth(), logo.getHeight());
            if (!logoFits(logo, minWidth, minHeight, maxWidth, maxHeight)) {
                log.trace("Size Mismatch");
                continue;
            }
            log.debug("Returning logo from UIInfo, ({} x {}) : {}", logo.getWidth(), logo.getHeight(), logo.getURL());
            return logo.getURL();
        }
        return null;
    }

    /**
     * Get the Logo (or null). We apply the languages and the supplied lengths.
     * 
     * @param minWidth the minimum width to allow.
     * @param minHeight the minimum height to allow.
     * @param maxWidth the maximum width to allow.
     * @param maxHeight the maximum height to allow.
     * @return an appropriate logo URL or null.
     */
    @Nullable public String getLogo(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        if (null == getRPUInfo() || null == rpUIInfo.getLogos() || rpUIInfo.getLogos().isEmpty()) {
            log.debug("No UIInfo or logos returning null");
            return null;
        }

        for (final String lang : getUsableLanguages()) {
            final String result = getLogoByLanguage(lang, minWidth, minHeight, maxWidth, maxHeight);
            if (null != result) {
                return policeURLLogo(result);
            }
        }
        final String result = getLogoNoLanguage(minWidth, minHeight, maxWidth, maxHeight);
        if (null != result) {
            return policeURLLogo(result);
        }
        return null;
    }

    /**
     * Get the Logo (or null). We apply the languages and the supplied lengths.
     * 
     * @param minWidth the minimum width to allow.
     * @param minHeight the minimum height to allow.
     * @param maxWidth the maximum width to allow.
     * @param maxHeight the maximum height to allow.
     * @return an appropriate logo URL or null.
     */
    @Nullable public String getLogo(String minWidth, String minHeight, String maxWidth, String maxHeight) {

        int minW;
        try {
            minW = Integer.parseInt(minWidth);
        } catch (NumberFormatException ex) {
            minW = Integer.MIN_VALUE;
        }

        int minH;
        try {
            minH = Integer.parseInt(minHeight);
        } catch (NumberFormatException ex) {
            minH = Integer.MIN_VALUE;
        }

        int maxW;
        try {
            maxW = Integer.parseInt(maxWidth);
        } catch (NumberFormatException ex) {
            maxW = Integer.MAX_VALUE;
        }

        int maxH;
        try {
            maxH = Integer.parseInt(maxHeight);
        } catch (NumberFormatException ex) {
            maxH = Integer.MAX_VALUE;
        }

        return getLogo(minW, minH, maxW, maxH);
    }

    /**
     * Get the Logo (or null). We apply the languages. Any size works.
     * 
     * @return an appropriate logo URL or null.
     */
    @Nullable public String getLogo() {
        return getLogo(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
}
