package com.setana.treenity.ui.ar

import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState
import com.google.ar.core.Session


/** 클라우드 앵커 사용을 도와주는 클래스, 코드랩 참조 */
class CloudAnchorManager {
    /** 클라우드 앵커의 통신 결과 이후 콜백 함수를 호출할 리스너 */
    interface CloudAnchorResultListener {
        /** 클라우드 앵커의 통신 결과 이후 호출되는 콜백 함수  */
        fun onCloudTaskComplete(anchor: Anchor?)
    }
    /** listener 는 HashMap 구조로 저장되어 onUpdate 에서 호출된다 */
    private val pendingAnchors = HashMap<Anchor, CloudAnchorResultListener>()

    /**
     * 서버에 앵커를 등록하여 클라우드 앵커 ID를 받아오는 함수
     * 통신 결과 listener 사용 가능
     */
    @Synchronized
    fun hostCloudAnchor(
        session: Session, anchor: Anchor?, ttl: Int, listener: CloudAnchorResultListener
    ): Anchor? {
        val newAnchor = session.hostCloudAnchorWithTtl(anchor, ttl)
        pendingAnchors[newAnchor] = listener
        return newAnchor
    }

    /**
     * 클라우드 앵커 아이디를 받아 세션에 앵커로 등록하는 함수
     * 통신 결과 listener 사용 가능
     */
    @Synchronized
    fun resolveCloudAnchor(
        session: Session, anchorId: String?, listener: CloudAnchorResultListener
    ): Anchor? {
        val newAnchor = session.resolveCloudAnchor(anchorId)
        pendingAnchors[newAnchor] = listener
        return newAnchor
    }

    /** 세션이 업데이트 될 때 실행되어야 하는 함수. 등록된 리스너를 차례로 실행해준다.  */
    @Synchronized
    fun onUpdate() {
        val iter: MutableIterator<Map.Entry<Anchor, CloudAnchorResultListener>> =
            pendingAnchors.entries.iterator()
        while (iter.hasNext()) {
            val (anchor, listener) = iter.next()
            if (isReturnableState(anchor.cloudAnchorState)) {
                listener.onCloudTaskComplete(anchor)
                iter.remove()
            }
        }
    }

    /** 리스너가 더이상 반응하지 않게 설정해줄 함수, 화면 Clear를 필요로 하는 경우(씨앗심기 취소 등)에 사용할 예정  */
    @Synchronized
    fun clearListeners() {
        pendingAnchors.clear()
    }
    /** 클라우드 앵커가 서버 통신 결과를 반환할 수 있는 상태임을 판독하는 함수 */
    companion object {
        private fun isReturnableState(cloudState: CloudAnchorState): Boolean {
            return when (cloudState) {
                CloudAnchorState.NONE, CloudAnchorState.TASK_IN_PROGRESS -> false
                else -> true
            }
        }
    }
}
