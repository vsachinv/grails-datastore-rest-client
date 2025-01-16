package grails.datastore.rest.client

import grails.plugins.*

class GrailsDatastoreRestClientGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
       def grailsVersion = "6.2.1  > *"
       // resources that are excluded from plugin packaging
       def pluginExcludes = [
           "grails-app/views/error.gsp"
       ]

       // TODO Fill in these fields
       def title = "Grails-datastore-rest-client" // Headline display name of the plugin
       def author = "Sachin Verma"
       def authorEmail = "sachin.verma@rxlogix.com"
       def description = '''\
  Created grails-datastore-rest-client plugin for grails 6.x upgraded dependencies fixes with code base taken from 
  https://github.com/grails/grails-data-mapping/blob/v6.1.13.RELEASE/grails-datastore-rest-client
   '''
   // URL to the plugin's documentation
       def documentation = "https://grails.github.io/GrailsDatastoreRestClient/"

       // Extra (optional) plugin metadata

       // License: one of 'APACHE', 'GPL2', 'GPL3'
       def license = "APACHE"

       // Details of company behind the plugin (if there is one)
       def organization = [ name: "RxLogix Corporation Pvt Ltd", url: "https://www.rxlogix.com/" ]

       // Any additional developers beyond the author specified above.
       def developers = [ [ name: "Sachin Verma", email: "sachin.verma@rxlogix.com" ]]

       // Location of the plugin's issue tracker.
       def issueManagement = [ system: "GitHub", url: "https://github.com/grails/GrailsDatastoreRestClient/issues" ]

       // Online location of the plugin's browseable source code.
   //    def scm = [ url: "https://github.com/grails/GrailsDatastoreRestClient" ]

       Closure doWithSpring() { {->
               // TODO Implement runtime spring config (optional)
           }
       }

       void doWithDynamicMethods() {
           // TODO Implement registering dynamic methods to classes (optional)
       }

       void doWithApplicationContext() {
           // TODO Implement post initialization spring config (optional)
       }

       void onChange(Map<String, Object> event) {
           // TODO Implement code that is executed when any artefact that this plugin is
           // watching is modified and reloaded. The event contains: event.source,
           // event.application, event.manager, event.ctx, and event.plugin.
       }

       void onConfigChange(Map<String, Object> event) {
           // TODO Implement code that is executed when the project configuration changes.
           // The event is the same as for 'onChange'.
       }

       void onShutdown(Map<String, Object> event) {
           // TODO Implement code that is executed when the application shuts down (optional)
       }
}