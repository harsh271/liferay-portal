/**
 * Copyright (c) 2000-2006 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portlet.calendar.service.persistence;

import com.liferay.portal.model.ModelListener;
import com.liferay.portal.util.PropsUtil;

import com.liferay.util.GetterUtil;
import com.liferay.util.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <a href="CalEventUtil.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Brian Wing Shun Chan
 *
 */
public class CalEventUtil {
	public static com.liferay.portlet.calendar.model.CalEvent create(
		java.lang.String eventId) {
		return getPersistence().create(eventId);
	}

	public static com.liferay.portlet.calendar.model.CalEvent remove(
		java.lang.String eventId)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		ModelListener listener = _getListener();

		if (listener != null) {
			listener.onBeforeRemove(findByPrimaryKey(eventId));
		}

		com.liferay.portlet.calendar.model.CalEvent calEvent = getPersistence()
																   .remove(eventId);

		if (listener != null) {
			listener.onAfterRemove(calEvent);
		}

		return calEvent;
	}

	public static com.liferay.portlet.calendar.model.CalEvent remove(
		com.liferay.portlet.calendar.model.CalEvent calEvent)
		throws com.liferay.portal.SystemException {
		ModelListener listener = _getListener();

		if (listener != null) {
			listener.onBeforeRemove(calEvent);
		}

		calEvent = getPersistence().remove(calEvent);

		if (listener != null) {
			listener.onAfterRemove(calEvent);
		}

		return calEvent;
	}

	public static com.liferay.portlet.calendar.model.CalEvent update(
		com.liferay.portlet.calendar.model.CalEvent calEvent)
		throws com.liferay.portal.SystemException {
		ModelListener listener = _getListener();
		boolean isNew = calEvent.isNew();

		if (listener != null) {
			if (isNew) {
				listener.onBeforeCreate(calEvent);
			}
			else {
				listener.onBeforeUpdate(calEvent);
			}
		}

		calEvent = getPersistence().update(calEvent);

		if (listener != null) {
			if (isNew) {
				listener.onAfterCreate(calEvent);
			}
			else {
				listener.onAfterUpdate(calEvent);
			}
		}

		return calEvent;
	}

	public static com.liferay.portlet.calendar.model.CalEvent update(
		com.liferay.portlet.calendar.model.CalEvent calEvent,
		boolean saveOrUpdate) throws com.liferay.portal.SystemException {
		ModelListener listener = _getListener();
		boolean isNew = calEvent.isNew();

		if (listener != null) {
			if (isNew) {
				listener.onBeforeCreate(calEvent);
			}
			else {
				listener.onBeforeUpdate(calEvent);
			}
		}

		calEvent = getPersistence().update(calEvent, saveOrUpdate);

		if (listener != null) {
			if (isNew) {
				listener.onAfterCreate(calEvent);
			}
			else {
				listener.onAfterUpdate(calEvent);
			}
		}

		return calEvent;
	}

	public static com.liferay.portlet.calendar.model.CalEvent findByPrimaryKey(
		java.lang.String eventId)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		return getPersistence().findByPrimaryKey(eventId);
	}

	public static com.liferay.portlet.calendar.model.CalEvent fetchByPrimaryKey(
		java.lang.String eventId) throws com.liferay.portal.SystemException {
		return getPersistence().fetchByPrimaryKey(eventId);
	}

	public static java.util.List findByGroupId(long groupId)
		throws com.liferay.portal.SystemException {
		return getPersistence().findByGroupId(groupId);
	}

	public static java.util.List findByGroupId(long groupId, int begin, int end)
		throws com.liferay.portal.SystemException {
		return getPersistence().findByGroupId(groupId, begin, end);
	}

	public static java.util.List findByGroupId(long groupId, int begin,
		int end, com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException {
		return getPersistence().findByGroupId(groupId, begin, end, obc);
	}

	public static com.liferay.portlet.calendar.model.CalEvent findByGroupId_First(
		long groupId, com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		return getPersistence().findByGroupId_First(groupId, obc);
	}

	public static com.liferay.portlet.calendar.model.CalEvent findByGroupId_Last(
		long groupId, com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		return getPersistence().findByGroupId_Last(groupId, obc);
	}

	public static com.liferay.portlet.calendar.model.CalEvent[] findByGroupId_PrevAndNext(
		java.lang.String eventId, long groupId,
		com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		return getPersistence().findByGroupId_PrevAndNext(eventId, groupId, obc);
	}

	public static java.util.List findByG_T(long groupId, java.lang.String type)
		throws com.liferay.portal.SystemException {
		return getPersistence().findByG_T(groupId, type);
	}

	public static java.util.List findByG_T(long groupId, java.lang.String type,
		int begin, int end) throws com.liferay.portal.SystemException {
		return getPersistence().findByG_T(groupId, type, begin, end);
	}

	public static java.util.List findByG_T(long groupId, java.lang.String type,
		int begin, int end, com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException {
		return getPersistence().findByG_T(groupId, type, begin, end, obc);
	}

	public static com.liferay.portlet.calendar.model.CalEvent findByG_T_First(
		long groupId, java.lang.String type,
		com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		return getPersistence().findByG_T_First(groupId, type, obc);
	}

	public static com.liferay.portlet.calendar.model.CalEvent findByG_T_Last(
		long groupId, java.lang.String type,
		com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		return getPersistence().findByG_T_Last(groupId, type, obc);
	}

	public static com.liferay.portlet.calendar.model.CalEvent[] findByG_T_PrevAndNext(
		java.lang.String eventId, long groupId, java.lang.String type,
		com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		return getPersistence().findByG_T_PrevAndNext(eventId, groupId, type,
			obc);
	}

	public static java.util.List findByG_R(long groupId, boolean repeating)
		throws com.liferay.portal.SystemException {
		return getPersistence().findByG_R(groupId, repeating);
	}

	public static java.util.List findByG_R(long groupId, boolean repeating,
		int begin, int end) throws com.liferay.portal.SystemException {
		return getPersistence().findByG_R(groupId, repeating, begin, end);
	}

	public static java.util.List findByG_R(long groupId, boolean repeating,
		int begin, int end, com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException {
		return getPersistence().findByG_R(groupId, repeating, begin, end, obc);
	}

	public static com.liferay.portlet.calendar.model.CalEvent findByG_R_First(
		long groupId, boolean repeating,
		com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		return getPersistence().findByG_R_First(groupId, repeating, obc);
	}

	public static com.liferay.portlet.calendar.model.CalEvent findByG_R_Last(
		long groupId, boolean repeating,
		com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		return getPersistence().findByG_R_Last(groupId, repeating, obc);
	}

	public static com.liferay.portlet.calendar.model.CalEvent[] findByG_R_PrevAndNext(
		java.lang.String eventId, long groupId, boolean repeating,
		com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException, 
			com.liferay.portlet.calendar.NoSuchEventException {
		return getPersistence().findByG_R_PrevAndNext(eventId, groupId,
			repeating, obc);
	}

	public static java.util.List findAll()
		throws com.liferay.portal.SystemException {
		return getPersistence().findAll();
	}

	public static java.util.List findAll(int begin, int end)
		throws com.liferay.portal.SystemException {
		return getPersistence().findAll(begin, end);
	}

	public static java.util.List findAll(int begin, int end,
		com.liferay.portal.kernel.util.OrderByComparator obc)
		throws com.liferay.portal.SystemException {
		return getPersistence().findAll(begin, end, obc);
	}

	public static void removeByGroupId(long groupId)
		throws com.liferay.portal.SystemException {
		getPersistence().removeByGroupId(groupId);
	}

	public static void removeByG_T(long groupId, java.lang.String type)
		throws com.liferay.portal.SystemException {
		getPersistence().removeByG_T(groupId, type);
	}

	public static void removeByG_R(long groupId, boolean repeating)
		throws com.liferay.portal.SystemException {
		getPersistence().removeByG_R(groupId, repeating);
	}

	public static void removeAll() throws com.liferay.portal.SystemException {
		getPersistence().removeAll();
	}

	public static int countByGroupId(long groupId)
		throws com.liferay.portal.SystemException {
		return getPersistence().countByGroupId(groupId);
	}

	public static int countByG_T(long groupId, java.lang.String type)
		throws com.liferay.portal.SystemException {
		return getPersistence().countByG_T(groupId, type);
	}

	public static int countByG_R(long groupId, boolean repeating)
		throws com.liferay.portal.SystemException {
		return getPersistence().countByG_R(groupId, repeating);
	}

	public static int countAll() throws com.liferay.portal.SystemException {
		return getPersistence().countAll();
	}

	public static void initDao() {
		getPersistence().initDao();
	}

	public static CalEventPersistence getPersistence() {
		return _getUtil()._persistence;
	}

	public void setPersistence(CalEventPersistence persistence) {
		_persistence = persistence;
	}

	private static CalEventUtil _getUtil() {
		if (_util == null) {
			_util = (CalEventUtil)com.liferay.portal.kernel.bean.BeanLocatorUtil.locate(_UTIL);
		}

		return _util;
	}

	private static ModelListener _getListener() {
		if (Validator.isNotNull(_LISTENER)) {
			try {
				return (ModelListener)Class.forName(_LISTENER).newInstance();
			}
			catch (Exception e) {
				_log.error(e);
			}
		}

		return null;
	}

	private static final String _UTIL = CalEventUtil.class.getName();
	private static final String _LISTENER = GetterUtil.getString(PropsUtil.get(
				"value.object.listener.com.liferay.portlet.calendar.model.CalEvent"));
	private static Log _log = LogFactory.getLog(CalEventUtil.class);
	private static CalEventUtil _util;
	private CalEventPersistence _persistence;
}