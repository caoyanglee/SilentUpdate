package www.weimu.io.silentupdate.strategy

/**
 * Author:你需要一台永动机
 * Date:2018/7/17 18:17
 * Description:
 */
internal interface StrategyAction {

    //检查更新的URL
    fun checkUpdateUrl(url: String)
}