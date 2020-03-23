package www.weimu.io.silentupdatedemo

import android.content.Context
import android.widget.Toast

/**
 * Author:你需要一台永动机
 * Date:2020/3/22 20:00
 * Description:
 */
fun Context.toast(text:String){
    Toast.makeText(this,text, Toast.LENGTH_SHORT).show()
}