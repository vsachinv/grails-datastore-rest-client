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
package grails.plugins.rest.client.async

import grails.async.DelegateAsync
import grails.plugins.rest.client.RestBuilder
import org.springframework.web.client.RestTemplate

/**
 * Asynchronous version of the {@link RestBuilder} class. Uses the {@DelegateAsync} annotation to provide the same API
 *
 * @author Graeme Rocher
 * @since 1.0
 */
class AsyncRestBuilder {

    @DelegateAsync RestBuilder restBuilder

    AsyncRestBuilder(Map settings) {
        this.restBuilder = new RestBuilder(settings)
    }
    AsyncRestBuilder() {
        this.restBuilder = new RestBuilder()
    }

    AsyncRestBuilder(RestBuilder restBuilder) {
        this.restBuilder = restBuilder
    }

    RestTemplate getRestTemplate() {
        return restBuilder.restTemplate
    }
}