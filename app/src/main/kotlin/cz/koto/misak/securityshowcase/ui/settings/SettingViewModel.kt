package cz.koto.misak.securityshowcase.ui.profile.settings

import android.databinding.ObservableBoolean
import com.strv.keystorecompat.CredentialsKeystoreProvider
import com.strv.keystorecompat.utility.showLockScreenSettings
import cz.koto.misak.securityshowcase.databinding.FragmentSettingsBinding
import cz.koto.misak.securityshowcase.storage.CredentialStorage
import cz.koto.misak.securityshowcase.ui.BaseViewModel
import cz.koto.misak.securityshowcase.utility.Logcat
import cz.koto.misak.securityshowcase.utility.runOnLollipop
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class SettingViewModel : BaseViewModel<FragmentSettingsBinding>() {

    val androidSecurityAvailable = ObservableBoolean(false)
    val androidSecuritySelectable = ObservableBoolean(false)

    override fun onViewModelCreated() {
        super.onViewModelCreated()
    }

    override fun onViewAttached(firstAttachment: Boolean) {
        super.onViewAttached(firstAttachment)

        setVisibility()


        runOnLollipop {
            binding.settingsAndroidSecuritySwitch.isChecked = CredentialsKeystoreProvider.hasCredentialsLoadable()
            binding.settingsAndroidSecuritySwitch.setOnCheckedChangeListener { switch, b ->
                if (b) {
                    binding.settingsAndroidSecuritySwitch.isEnabled = false
                    Flowable.fromCallable {
                        CredentialsKeystoreProvider.storeCredentials(
                                "${CredentialStorage.getUserName()};${CredentialStorage.getPassword()}",
                                { Logcat.e("Store credentials failed!") })
                    }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({}, {
                                it.printStackTrace()
                                binding.settingsAndroidSecuritySwitch.isEnabled = true
                            }, {
                                binding.settingsAndroidSecuritySwitch.isEnabled = true
                                /* DEV test to load stored credentials (don't forget to increase setUserAuthenticationValidityDurationSeconds() to fulfill this test!) */
                                //CredentialsKeystoreProvider.loadCredentials({ loaded -> Logcat.w("LOAD test %s", loaded) }, { Logcat.e("LOAD test FAILURE") }, false)
                            })
                } else {
                    CredentialsKeystoreProvider.clearCredentials()
                }
            }
        }
    }

    private fun setVisibility() {
        runOnLollipop {
            androidSecurityAvailable.set(CredentialsKeystoreProvider.isProviderAvailable())
            androidSecuritySelectable.set(CredentialsKeystoreProvider.isSecurityEnabled())
        }
    }

    override fun onResume() {
        super.onResume()
        setVisibility()
    }

    fun onClickSecuritySettings() {
        showLockScreenSettings(context)
    }
}