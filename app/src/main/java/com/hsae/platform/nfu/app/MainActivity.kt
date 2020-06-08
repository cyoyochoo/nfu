package com.hsae.platform.nfu.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.material.snackbar.Snackbar
import com.hsae.platform.nfu.NFU
import com.hsae.platform.nfu.wrapper.message.*
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val scanCode = 38

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == scanCode && resultCode == Activity.RESULT_OK) {
            data?.apply {
                getStringExtra("result")?.let {
                    NFU.linkCheck(it).autoDispose(lifecycle).subscribe { msg ->
                        showLinkCheckDlg(msg)
                    }
                }
            }
        }
    }

    private fun bindClick() {
        offlineCheck.setOnClickListener {
            NFU.offlineCheck().autoDispose(lifecycle).subscribe { msg ->
                showOfflineCheckDlg(msg)
            }
        }

        linkCheck.setOnClickListener {
            RxPermissions(this)
                .request(Manifest.permission.CAMERA)
                .autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle, Lifecycle.Event.ON_DESTROY))
                .subscribe {
                    if (it)
                        startActivityForResult(
                            Intent(this, ScanActivity::class.java),
                            scanCode
                        )
                    else
                        Snackbar.make(
                            linkCheck,
                            "您拒绝了相机权限",
                            Snackbar.LENGTH_LONG
                        ).show()
                }
        }
    }

    private var offlineCheckDlg: AlertDialog? = null
    private var preDownloadBtn: Button? = null
    private var preDownloadDlg: AlertDialog? = null
    private var linkCheckDlg: AlertDialog? = null
    private var upgradeBtn: Button? = null
    private var upgradeDlg: AlertDialog? = null

    private fun showOfflineCheckDlg(msg: OfflineCheckMsg) {
        offlineCheckDlg?.let { if (!it.isShowing) it.show() } ?: run {
            offlineCheckDlg = AlertDialog.Builder(this)
                .setTitle("OfflineCheck")
                .setMessage("")
                .setNegativeButton("Dismiss", null)
                .setPositiveButton("PreDownload", null)
                .setCancelable(false).show()
            preDownloadBtn = offlineCheckDlg?.getButton(AlertDialog.BUTTON_POSITIVE)
            preDownloadBtn?.setOnClickListener {
                offlineCheckDlg?.dismiss()
                NFU.offlineDownload().autoDispose(lifecycle).subscribe { showOfflineDownloadDlg(it) }
            }
            preDownloadBtn?.isEnabled = false
        }
        when (msg.state) {
            OfflineCheckState.start -> offlineCheckDlg?.setMessage("Start")
            OfflineCheckState.error -> offlineCheckDlg?.setMessage("Error: \n${msg.detail}")
            OfflineCheckState.end -> {
                preDownloadBtn?.isEnabled = true
                offlineCheckDlg?.setMessage("End: \n${msg.version.toString()}")
            }
            else -> offlineCheckDlg?.setMessage("Illegal")
        }
    }

    private fun showOfflineDownloadDlg(msg: DownloadMsg) {
        preDownloadDlg?.let { if (!it.isShowing) it.show() } ?: run {
            preDownloadDlg = AlertDialog.Builder(this)
                .setTitle("PreDownload")
                .setMessage("")
                .setNegativeButton("Dismiss", null)
                .setCancelable(false).show()
        }
        when (msg.state) {
            DownloadState.start -> preDownloadDlg?.setMessage("Start")
            DownloadState.downloading -> preDownloadDlg?.setMessage("Progress --> ${msg.progress} / 100")
            DownloadState.error -> preDownloadDlg?.setMessage("Error: \n${msg.detail}")
            DownloadState.end -> preDownloadDlg?.setMessage("End")
            else -> preDownloadDlg?.setMessage("Illegal")
        }
    }

    private fun showLinkCheckDlg(msg: LinkCheckMsg) {
        linkCheckDlg?.let { if (!it.isShowing) it.show() } ?: run {
            linkCheckDlg = AlertDialog.Builder(this)
                .setTitle("LinkCheck")
                .setMessage("")
                .setNegativeButton("Dismiss", null)
                .setPositiveButton("Upgrade", null)
                .setCancelable(false).show()
            upgradeBtn = linkCheckDlg?.getButton(AlertDialog.BUTTON_POSITIVE)
            upgradeBtn?.setOnClickListener {
                linkCheckDlg?.dismiss()
                NFU.linkUpgrade().autoDispose(lifecycle).subscribe { showLinkUpgradeDlg(it) }
            }
            upgradeBtn?.isEnabled = false
        }
        when (msg.state) {
            LinkCheckState.start -> linkCheckDlg?.setMessage("Start")
            LinkCheckState.linking -> linkCheckDlg?.setMessage("Linking: \n${msg.detail}")
            LinkCheckState.linked -> linkCheckDlg?.setMessage("Linked")
            LinkCheckState.checking -> linkCheckDlg?.setMessage("Checking")
            LinkCheckState.error -> linkCheckDlg?.setMessage("Error: \n${msg.detail}")
            LinkCheckState.end -> {
                upgradeBtn?.isEnabled = true
                linkCheckDlg?.setMessage("End: \n${msg.version.toString()}")
            }
            else -> linkCheckDlg?.setMessage("Illegal")
        }
    }

    private fun showLinkUpgradeDlg(msg: UpgradeMsg) {
        upgradeDlg?.let { if (!it.isShowing) it.show() } ?: run {
            upgradeDlg = AlertDialog.Builder(this)
                .setTitle("LinkUpgrade")
                .setMessage("")
                .setNegativeButton("Dismiss", null)
                .setCancelable(false).show()
        }
        when (msg.state) {
            UpgradeState.start -> upgradeDlg?.setMessage("Start")
            UpgradeState.downloading -> upgradeDlg?.setMessage("Download --> ${msg.progress} / 100")
            UpgradeState.downloaded -> upgradeDlg?.setMessage("Downloaded")
            UpgradeState.transmitting -> upgradeDlg?.setMessage("Transmit --> ${msg.progress} / 100")
            UpgradeState.transmitted -> upgradeDlg?.setMessage("Transmitted")
            UpgradeState.error -> upgradeDlg?.setMessage("Error: \n${msg.detail}")
            UpgradeState.end -> upgradeDlg?.setMessage("End")
            else -> upgradeDlg?.setMessage("Illegal")
        }
    }
}
