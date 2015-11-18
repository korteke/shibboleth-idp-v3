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

package net.shibboleth.idp.profile.spring.resource.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.resource.SVNBasicAuthenticationManager;
import net.shibboleth.ext.spring.resource.SVNResource;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.auth.SVNUserNameAuthentication;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.w3c.dom.Element;

/**
 * Parser for resources of type SVNResource.
 */
public class SVNResourceParser extends AbstractSingleBeanDefinitionParser {

    /** Schema type. */
    public static final QName ELEMENT_NAME = new QName(ResourceNamespaceHandler.NAMESPACE, "SVNResource");

    /** Configuration element attribute {@value} which holds the URL to the remote repository. */
    public static final String REPOSITORY_URL_ATTRIB_NAME = "repositoryURL";

    /** Configuration element attribute {@value} which holds the timeout used when connecting to the SVN server. */
    public static final String CTX_TIMEOUT_ATTRIB_NAME = "connectionTimeout";

    /** Configuration element attribute {@value} which holds the timeout used when reading from the SVN server. */
    public static final String READ_TIMEOUT_ATTRIB_NAME = "readTimeout";

    /** Configuration element attribute {@value} which holds the path to the working copy directory. */
    public static final String WORKING_COPY_DIR_ATTRIB_NAME = "workingCopyDirectory";

    /** Configuration element attribute {@value} which holds the path to the working copy directory. */
    public static final String REVISION_ATTRIB_NAME = "revision";

    /**
     * Configuration element attribute {@value} which holds the path to the resource file represented by the SVN
     * resource.
     */
    public static final String RESOURCE_FILE_ATTRIB_NAME = "resourceFile";

    /** Configuration element attribute {@value} which holds the SVN username. */
    public static final String USERNAME_ATTRIB_NAME = "username";

    /** Configuration element attribute {@value} which holds the SVN password. */
    public static final String PASSWORD_ATTRIB_NAME = "password";

    /**
     * Configuration element attribute {@value} which holds the hostname of the proxy server used when connecting to the
     * SVN server.
     */
    public static final String PROXY_HOST_ATTRIB_NAME = "proxyHost";

    /**
     * Configuration element attribute {@value} which holds the port of the proxy server used when connecting to the SVN
     * server.
     */
    public static final String PROXY_PORT_ATTRIB_NAME = "proxyPort";

    /**
     * Configuration element attribute {@value} which holds the username used with the proxy server used when connecting
     * to the SVN server.
     */
    public static final String PROXY_USERNAME_ATTRIB_NAME = "proxyUsername";

    /**
     * Configuration element attribute {@value} which holds the password used with the proxy server used when connecting
     * to the SVN server.
     */
    public static final String PROXY_PASSWORD_ATTRIB_NAME = "proxyPassword";

    /** Logger.*/
    private final Logger log = LoggerFactory.getLogger(SVNResourceParser.class);

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(final Element element) {
        return SVNResource.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(final Element element, final ParserContext parserContext,
            final BeanDefinitionBuilder builder) {
        builder.setLazyInit(true);
        builder.setDestroyMethodName("destroy");
        builder.setInitMethodName("initialize");

        super.doParse(element, parserContext, builder);
        ResourceNamespaceHandler.noFilters(element, parserContext.getReaderContext());

        final BeanDefinition url = getRespositoryUrl(element, parserContext);

        builder.addConstructorArgValue(buildClientManager(element, url, parserContext));

        builder.addConstructorArgValue(url);

        builder.addConstructorArgValue(getAttribute(element, WORKING_COPY_DIR_ATTRIB_NAME, parserContext));

        final String revision = StringSupport.trimOrNull(getAttribute(element, REVISION_ATTRIB_NAME, parserContext));
        
        if (null == revision) {
            builder.addConstructorArgValue(-1);
        } else{
            builder.addConstructorArgValue(revision);
        }

        builder.addConstructorArgValue(getAttribute(element, RESOURCE_FILE_ATTRIB_NAME, parserContext));
    }

    /**
     * Builds the SVN client manager from the given configuration options.
     * 
     * @param element element bearing the configuration options
     * @param url the url for the repository
     * @param parserContext the context
     * 
     * @return the SVN client manager
     */
    @Nonnull protected BeanDefinition buildClientManager(final Element element, final BeanDefinition url,
            final ParserContext parserContext) {
        List<BeanDefinition> authnMethods = new ManagedList<>();

        final String username = getAttribute(element, USERNAME_ATTRIB_NAME, parserContext);
        if (username != null) {
            final BeanDefinitionBuilder usernameBuilder =
                    BeanDefinitionBuilder.genericBeanDefinition(SVNUserNameAuthentication.class);
            usernameBuilder.setLazyInit(true);
            
            usernameBuilder.addConstructorArgValue(username);
            usernameBuilder.addConstructorArgValue(false);
            usernameBuilder.addConstructorArgValue(url);
            usernameBuilder.addConstructorArgValue(false);

            authnMethods.add(usernameBuilder.getBeanDefinition());

            final String password = getAttribute(element, PASSWORD_ATTRIB_NAME, parserContext);
            if (password != null) {
                final BeanDefinitionBuilder passwordBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition(SVNPasswordAuthentication.class);
                passwordBuilder.setLazyInit(true);
                
                passwordBuilder.addConstructorArgValue(username);
                passwordBuilder.addConstructorArgValue(password);
                passwordBuilder.addConstructorArgValue(false);
                passwordBuilder.addConstructorArgValue(url);
                passwordBuilder.addConstructorArgValue(false);

                authnMethods.add(passwordBuilder.getBeanDefinition());
            }
        }

        final String proxyHost = getAttribute(element, PROXY_HOST_ATTRIB_NAME, parserContext);
        final String proxyPort = getAttribute(element, PROXY_PORT_ATTRIB_NAME, parserContext);
        final String proxyUser = getAttribute(element, PROXY_USERNAME_ATTRIB_NAME, parserContext);
        final String proxyPassword = getAttribute(element, PROXY_PASSWORD_ATTRIB_NAME, parserContext, false);

        BeanDefinitionBuilder authnManager =
                BeanDefinitionBuilder.genericBeanDefinition(SVNBasicAuthenticationManager.class);
        authnManager.setLazyInit(true);

        authnManager.addConstructorArgValue(authnMethods);
        if (proxyHost != null) {
            authnManager.addPropertyValue("proxyHost", proxyHost);
            authnManager.addPropertyValue("proxyPort", proxyPort);
            authnManager.addPropertyValue("proxyUserName", proxyUser);
            authnManager.addPropertyValue("proxyPassword", proxyPassword);
        }
        final String connectionTimeout = getAttribute(element, CTX_TIMEOUT_ATTRIB_NAME, parserContext);
        if (null != connectionTimeout) {
            log.warn("SVN resource definition attribute '{}' is ignored.", CTX_TIMEOUT_ATTRIB_NAME);
        }
        final String readTimeout = getAttribute(element, READ_TIMEOUT_ATTRIB_NAME, parserContext);
        if (null != readTimeout) {
            log.warn("SVN resource definition attribute '{}' is ignored.", READ_TIMEOUT_ATTRIB_NAME);
        }

        BeanDefinitionBuilder clientManager = BeanDefinitionBuilder.genericBeanDefinition(SVNClientManager.class);
        clientManager.setLazyInit(true);
        clientManager.setFactoryMethod("newInstance");
        clientManager.addPropertyValue("authenticationManager", authnManager.getBeanDefinition());
        return clientManager.getBeanDefinition();
    }

    /**
     * Gets the value of the {@value #REPOSITORY_URL_ATTRIB_NAME} attribute.
     * 
     * @param element resource configuration element
     * @param parserContext the context to provide support to error functions. 
     * 
     * @return value of the attribute
     * 
     */
    protected BeanDefinition getRespositoryUrl(final Element element, final ParserContext parserContext) {

        final String value = getAttribute(element, REPOSITORY_URL_ATTRIB_NAME, parserContext);
        
        if (null == value) {
            error("SVN resource definition.  No " + REPOSITORY_URL_ATTRIB_NAME + " specified", parserContext);
        }
        
        final BeanDefinitionBuilder urlBuilder = BeanDefinitionBuilder.genericBeanDefinition(SVNURL.class);
        urlBuilder.setLazyInit(true);
        urlBuilder.setFactoryMethod("parseURIDecoded");
        urlBuilder.addConstructorArgValue(value);
        return urlBuilder.getBeanDefinition();
    }

    /**
     * Gets the value of the supplied attribute.
     * 
     * @param element resource configuration element
     * @param parserContext the parser context. Used to provide the failing location.
     * @param attributeName the attribute to look up.
     * @param trim whether to trim leading and trailing spaces.
     * @return value of the attribute
     * 
     */
    @Nullable protected String getAttribute(final Element element, final String attributeName,
            final ParserContext parserContext, final boolean trim) {
        if (element.hasAttributeNS(null, attributeName)) {
            String value = element.getAttributeNS(null, attributeName);
            if (trim) {
                value = StringSupport.trimOrNull(value);
            }
            if (value == null) {
                error("SVN resource definition attribute '" + attributeName + "' may not be an empty string",
                        parserContext);
            }
            return value;
        }
        return null;
    }

    /**
     * Gets the value of the supplied attribute.
     * 
     * @param element resource configuration element
     * @param parserContext the parser context. Used to provide the failing location.
     * @param attributeName the attribute to look up.
     * @return value of the attribute
     * 
     */
    @Nullable protected String getAttribute(final Element element, final String attributeName,
            final ParserContext parserContext) {
        return getAttribute(element, attributeName, parserContext, true);
    }

    /**
     * Cause an error with the provided message.
     * 
     * @param message the message.
     * @param parserContext the parser context. Used to provide the failing location.
     * @throws BeanDefinitionParsingException if we encounter a filter
     */
    protected void error(final String message, final ParserContext parserContext) {
        log.error(message);
        throw new BeanDefinitionParsingException(new Problem(message, new Location(parserContext.getReaderContext()
                .getResource())));
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }
    
}
