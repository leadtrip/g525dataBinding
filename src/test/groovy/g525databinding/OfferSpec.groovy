package g525databinding

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class OfferSpec extends Specification implements DomainUnitTest<Offer> {

    void "test all good"() {
        given:
            def offer1 = new Offer(name: 'offer1')
            def translation1 = new Translation(name: 'translation1', locale: Locale.CANADA)
            offer1.addToTranslations(translation1)
        when:
            offer1.save(failOnError: true)
        then:
            !offer1.hasErrors()
    }
}
