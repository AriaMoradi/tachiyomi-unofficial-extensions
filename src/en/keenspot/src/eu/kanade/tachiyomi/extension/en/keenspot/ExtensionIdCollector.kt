package eu.kanade.tachiyomi.extension.en.keenspot

class ExtensionIdCollector {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
//            Class.forName("eu.kanade.tachiyomi.extension.en.keenspot.TwoKinds").getField("name").
            print(TwoKinds::name.get()
        }
    }
}
