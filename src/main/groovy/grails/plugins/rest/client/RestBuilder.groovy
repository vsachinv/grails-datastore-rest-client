/* Copyright (C) 2013 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugins.rest.client

import org.grails.web.converters.configuration.ConverterConfiguration
import org.grails.web.converters.marshaller.json.ArrayMarshaller
import org.grails.web.converters.marshaller.json.ByteArrayMarshaller
import org.grails.web.converters.marshaller.json.CollectionMarshaller
import org.grails.web.converters.marshaller.json.GroovyBeanMarshaller
import org.grails.web.converters.marshaller.json.MapMarshaller

import static org.springframework.http.HttpMethod.*

import java.net.Proxy

import grails.converters.JSON
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.rest.client.json.GsonHttpMessageConverter
import org.grails.datastore.gorm.rest.client.json.JsonHttpMessageConverter
import org.grails.datastore.gorm.rest.client.utils.GrailsConverterHttpMessageConverter
import org.grails.datastore.gorm.rest.client.utils.NullSafeStringHttpMessageConverter
import org.grails.datastore.gorm.rest.client.utils.WritableHttpMessageConverter
import org.grails.datastore.gorm.rest.client.xml.GPathXmlHttpMessageConverter
import org.grails.web.converters.configuration.ConvertersConfigurationHolder
import org.grails.web.converters.configuration.ConvertersConfigurationInitializer
import org.grails.web.converters.configuration.DefaultConverterConfiguration
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.ClassUtils
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

/**
 * Main API entry to the synchronous version of the REST low-level client API
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class RestBuilder {

    private static final GroovyBeanMarshaller DEFAULT_GROOVY_MARSHALLER = new GroovyBeanMarshaller()
    private static final MapMarshaller DEFAULT_MAP_MARSHALLER = new MapMarshaller()
    private static final ArrayMarshaller DEFAULT_ARRAY_MARSHALLER = new ArrayMarshaller()
    private static final ByteArrayMarshaller DEFAULT_BYTE_MARSHALLER = new ByteArrayMarshaller()
    private static final CollectionMarshaller DEFAULT_COLLECTION_MARSHALLER = new CollectionMarshaller()

    RestTemplate restTemplate = new RestTemplate()

    RestBuilder(Map settings = [:]) {

        Boolean registerConverters = settings.containsKey('registerConverters') ? settings.registerConverters : true

        if (registerConverters) {
            def currentConfiguration = ConvertersConfigurationHolder.getConverterConfiguration(JSON)
            if(currentConfiguration instanceof DefaultConverterConfiguration) {
                // init manually
                DefaultConverterConfiguration defaultConfig = ((DefaultConverterConfiguration)currentConfiguration)
                if(!defaultConfig.orderedObjectMarshallers.contains(DEFAULT_GROOVY_MARSHALLER)) {
                    defaultConfig.registerObjectMarshaller(DEFAULT_GROOVY_MARSHALLER, -1)
                    defaultConfig.registerObjectMarshaller(DEFAULT_MAP_MARSHALLER, -1)
                    defaultConfig.registerObjectMarshaller(DEFAULT_ARRAY_MARSHALLER, -1)
                    defaultConfig.registerObjectMarshaller(DEFAULT_BYTE_MARSHALLER, -1)
                    defaultConfig.registerObjectMarshaller(DEFAULT_COLLECTION_MARSHALLER, -1)
                }
            }
        }

        Proxy proxyFromSystemProperties = getProxyForSystemProperties()
        if (proxyFromSystemProperties && settings.proxy == null) {
            settings.proxy = proxyFromSystemProperties
        }

        if (settings.proxy instanceof Map) {
            def ps = ((Map)settings.proxy).entrySet().iterator().next()
            if (ps.value) {
                def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ps.key.toString(), ps.value.toString().toInteger()))
                settings.proxy = proxy
            }
        }

        final customRequestFactory = new SimpleClientHttpRequestFactory()
        final metaClass = GroovySystem.metaClassRegistry.getMetaClass(SimpleClientHttpRequestFactory)
        for(key in settings.keySet()) {
            final prop = key.toString()
            if(customRequestFactory.hasProperty(prop)) {
                metaClass.setProperty(customRequestFactory, prop, settings.get(key))
            }
        }
        restTemplate.setRequestFactory(customRequestFactory)
        registerMessageConverters(restTemplate)
    }

    static Proxy getProxyForSystemProperties() {
        def proxyHost = System.getProperty("http.proxyHost")
        def proxyPort = System.getProperty("http.proxyPort")

        Proxy proxyFromSystemProperties = null
        if (proxyHost && proxyPort) {
            proxyFromSystemProperties = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort.toInteger()))
        }
        proxyFromSystemProperties
    }

    RestBuilder(RestTemplate restTemplate) {
        this.restTemplate = restTemplate
        registerMessageConverters(restTemplate)
    }
    /**
     * Issues a GET request and returns the response in the most appropriate type
     * @param url The URL
     * @param url The closure customizer used to customize request attributes
     */
    RestResponse get(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, GET
    }

    /**
     * Issues a GET request and returns the response in the most appropriate type
     * @param url The URL
     * @paral urlVariables The urlVariables required by the URL pattern
     * @param url The closure customizer used to customize request attributes
     */
    RestResponse get(String url, Map<String, Object> urlVariables, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, GET, urlVariables
    }


    /**
     * Issues a PUT request and returns the response in the most appropriate type
     *
     * @param url The URL
     * @param customizer The clouser customizer
     */
    RestResponse put(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, PUT
    }

    /**
     * Issues a PUT request and returns the response in the most appropriate type
     *
     * @param url The URL
     * @param customizer The clouser customizer
     */
    RestResponse put(String url, Map<String, Object> urlVariables, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, PUT, urlVariables
    }

    /**
     * Issues a POST request and returns the response
     * @param url The URL
     * @param customizer (optional) The closure customizer
     */

    RestResponse post(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, POST
    }

    /**
     * Issues a POST request and returns the response
     * @param url The URL
     * @param customizer (optional) The closure customizer
     */

    RestResponse post(String url, Map<String, Object> urlVariables, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, POST, urlVariables
    }

    /**
     * Issues a PATCH request and returns the response
     * @param url The URL
     * @param customizer (optional) The closure customizer
     */

    RestResponse patch(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, PATCH
    }

    /**
     * Issues a PATCH request and returns the response
     * @param url The URL
     * @param customizer (optional) The closure customizer
     */

    RestResponse patch(String url, Map<String, Object> urlVariables, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, PATCH, urlVariables
    }

    /**
     * Issues DELETE a request and returns the response

     * @param url The URL
     * @param customizer (optional) The closure customizer
     */
    RestResponse delete(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, DELETE
    }

    /**
     * Issues DELETE a request and returns the response

     * @param url The URL
     * @param customizer (optional) The closure customizer
     */
    RestResponse delete(String url, Map<String, Object> urlVariables,@DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, DELETE, urlVariables
    }


    /**
     * Issues HEAD a request and returns the response

     * @param url The URL
     * @param customizer (optional) The closure customizer
     */
    RestResponse head(String url, Map<String, Object> urlVariables,@DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, HEAD, urlVariables
    }

    /**
     * Issues HEAD a request and returns the response

     * @param url The URL
     * @param customizer (optional) The closure customizer
     */
    RestResponse head(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, HEAD
    }

    /**
     * Issues OPTIONS a request and returns the response

     * @param url The URL
     * @param customizer (optional) The closure customizer
     */
    RestResponse options(String url, Map<String, Object> urlVariables,@DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, OPTIONS, urlVariables
    }

    /**
     * Issues OPTIONS a request and returns the response

     * @param url The URL
     * @param customizer (optional) The closure customizer
     */
    RestResponse options(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, OPTIONS
    }

    /**
     * Issues TRACE a request and returns the response

     * @param url The URL
     * @param customizer (optional) The closure customizer
     */
    RestResponse trace(String url, Map<String, Object> urlVariables,@DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, TRACE, urlVariables
    }

    /**
     * Issues TRACE a request and returns the response

     * @param url The URL
     * @param customizer (optional) The closure customizer
     */
    RestResponse trace(String url, @DelegatesTo(RequestCustomizer) Closure customizer = null) {
        doRequestInternal url, customizer, TRACE
    }

    protected RestResponse doRequestInternal(String url, Closure customizer, HttpMethod method, Map<String, Object> urlVariables = Collections.emptyMap()) {

        def requestCustomizer = new RequestCustomizer()
        if(urlVariables)
            requestCustomizer.urlVariables.putAll(urlVariables)
        if (customizer != null) {
            customizer.delegate = requestCustomizer
            customizer.resolveStrategy = Closure.DELEGATE_FIRST
            customizer.call()
        }

        try {
            ResponseEntity responseEntity = invokeRestTemplate(url, method, requestCustomizer)
            handleResponse(responseEntity)
        }
        catch (HttpStatusCodeException e) {
            return new RestResponse(new ResponseEntity(e.getResponseBodyAsString(), e.responseHeaders, e.statusCode))
        }
    }

    protected void registerMessageConverters(RestTemplate restTemplate) {
        final messageConverters = restTemplate.getMessageConverters()
        final stringConverter = messageConverters.find { HttpMessageConverter httpMessageConverter -> httpMessageConverter instanceof StringHttpMessageConverter }
        if(stringConverter) {
            messageConverters.remove(stringConverter)
        }
        if(ClassUtils.isPresent("org.springframework.http.converter.json.MappingJackson2HttpMessageConverter", getClass().getClassLoader())) {
            messageConverters.removeAll { HttpMessageConverter httpMessageConverter -> httpMessageConverter.getClass().name == "org.springframework.http.converter.json.MappingJackson2HttpMessageConverter"}
        }
        if(ClassUtils.isPresent("org.springframework.http.converter.json.MappingJacksonHttpMessageConverter", getClass().getClassLoader())) {
            messageConverters.removeAll { HttpMessageConverter httpMessageConverter -> httpMessageConverter.getClass().name == "org.springframework.http.converter.json.MappingJacksonHttpMessageConverter"}
        }
        if(ClassUtils.isPresent("org.springframework.http.converter.json.GsonHttpMessageConverter", getClass().getClassLoader())) {
            messageConverters.removeAll { HttpMessageConverter httpMessageConverter -> httpMessageConverter.getClass().name == "org.springframework.http.converter.json.GsonHttpMessageConverter"}
        }
        if(ClassUtils.isPresent("com.google.gson.Gson", getClass().getClassLoader())) {
            messageConverters.add(0, new GsonHttpMessageConverter())
        }
        messageConverters.add(0, new NullSafeStringHttpMessageConverter())
        messageConverters.add(0, new JsonHttpMessageConverter())
        messageConverters.add(0, new GPathXmlHttpMessageConverter())
        messageConverters.add(0, new GrailsConverterHttpMessageConverter())
        messageConverters.add(0, new WritableHttpMessageConverter())
    }


    protected ResponseEntity invokeRestTemplate(String url, HttpMethod method, RequestCustomizer requestCustomizer) {
        def responseEntity = restTemplate.exchange(url, method, requestCustomizer.createEntity(),
                requestCustomizer.acceptType, requestCustomizer.getUrlVariables())
        responseEntity
    }

    protected RestResponse handleResponse(ResponseEntity responseEntity) {
        return new RestResponse(responseEntity)
    }
}