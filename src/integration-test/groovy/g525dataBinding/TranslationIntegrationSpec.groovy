package g525dataBinding

import g525databinding.Offer
import g525databinding.Translation
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class TranslationIntegrationSpec extends Specification{

    void "test unique constraint violation"() {
        given:
            def offer1 = new Offer(name: 'offer1').save()
            def offer2 = new Offer(name: 'offer2').save()
            new Translation(name: 'translation1', locale: Locale.CANADA, offer: offer1).save()
        when: 'assigning a different offer to a new translation with same locale is fine'
            def translation2 = new Translation(name: 'translation2', locale: Locale.CANADA, offer: offer2).save()
        then:
            !translation2.hasErrors()
        when: 'assigning the same offer associated with an existing translation with the same locale hits the unique constraint'
            translation2.offer = offer1
            translation2.save()
        then:
            translation2.hasErrors()
            translation2.errors.getFieldError('offer').toString().contains('unique')
    }

    void "test unique constraint is fine for different locales"() {
        given:
            def offer1 = new Offer(name: 'offer1').save()
            new Translation(name: 'translation1', locale: Locale.CANADA, offer: offer1).save()
        when: 'create new translation with same offer but different locale is fine'
            def translation2 = new Translation(name: 'translation2', locale: Locale.UK, offer: offer1).save()
        then:
            !translation2.hasErrors()
        when: 'changing the locale of the second translation to the same as the first translation hits the unique constraint'
            translation2.locale = Locale.CANADA
            translation2.save()
        then:
            translation2.hasErrors()
            translation2.errors.getFieldError('offer').toString().contains('unique')
    }

    /**
     * This test shows why it's important to include the id of associated domains when updating a domain's properties
     * like - myDomain.properties = params
     * If the associated domain id is not present, grails will create a new instance, this may be what's required but
     * if an existing domain exists with the same parameters as is the case below this could be bad, below this is prevented
     * with a unique constraint but without it a new domain instance would be created with the same properties as an existing one.
     */
    void "test unique constraint update binding with properties"() {
        given:
            def offer1 = new Offer(name: 'offer1').save()
            def translationCanada = new Translation(name: 'translation1', locale: Locale.CANADA, offer: offer1).save()
            new Translation(name: 'translation2', locale: Locale.UK, offer: offer1).save()
            new Translation(name: 'translation3', locale: Locale.FRANCE, offer: offer1).save()
        when:
            offer1.validate()
        then:
            !offer1.hasErrors()
        when: 'attempting to update the translations fails because a duplicate Canadian locale would be created as we have not included an id in the params'
            def translation4 = [name: 'translation4', locale: Locale.GERMANY]
            def translation5 = [name: 'translation5', locale: Locale.US]
            def translation6 = [name: translationCanada.name, locale: translationCanada.locale]

            def updateMap = [translations: [translation4, translation5, translation6]]
            offer1.properties = updateMap
            offer1.save(flush: true)
        then:
            offer1.hasErrors()
        when: 'this time we specify the id of the existing translation with Canadian locale and the update succeeds'
            translation6.id = translationCanada.id
            offer1.properties = updateMap
            offer1.save(flush: true)
        then:
            !offer1.hasErrors()
            Translation.all.size() == 5
            offer1.translations.size() == 3
            offer1.translations*.locale.containsAll([Locale.GERMANY, Locale.US, Locale.CANADA])
    }
}
