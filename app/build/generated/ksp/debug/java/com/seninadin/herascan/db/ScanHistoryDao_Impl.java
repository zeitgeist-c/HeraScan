package com.seninadin.herascan.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.paging.PagingSource;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.paging.LimitOffsetPagingSource;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ScanHistoryDao_Impl implements ScanHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScanHistory> __insertionAdapterOfScanHistory;

  private final EntityDeletionOrUpdateAdapter<ScanHistory> __deletionAdapterOfScanHistory;

  public ScanHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScanHistory = new EntityInsertionAdapter<ScanHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `scan_history` (`id`,`title`,`dateMillis`,`pdfUri`,`thumbnailUri`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScanHistory entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindLong(3, entity.getDateMillis());
        if (entity.getPdfUri() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPdfUri());
        }
        if (entity.getThumbnailUri() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getThumbnailUri());
        }
      }
    };
    this.__deletionAdapterOfScanHistory = new EntityDeletionOrUpdateAdapter<ScanHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `scan_history` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScanHistory entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insertScan(final ScanHistory scanHistory,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfScanHistory.insert(scanHistory);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteScan(final ScanHistory scanHistory,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfScanHistory.handle(scanHistory);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScanHistory>> getAllScans() {
    final String _sql = "SELECT * FROM scan_history ORDER BY dateMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scan_history"}, new Callable<List<ScanHistory>>() {
      @Override
      @NonNull
      public List<ScanHistory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDateMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dateMillis");
          final int _cursorIndexOfPdfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "pdfUri");
          final int _cursorIndexOfThumbnailUri = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUri");
          final List<ScanHistory> _result = new ArrayList<ScanHistory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanHistory _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpDateMillis;
            _tmpDateMillis = _cursor.getLong(_cursorIndexOfDateMillis);
            final String _tmpPdfUri;
            if (_cursor.isNull(_cursorIndexOfPdfUri)) {
              _tmpPdfUri = null;
            } else {
              _tmpPdfUri = _cursor.getString(_cursorIndexOfPdfUri);
            }
            final String _tmpThumbnailUri;
            if (_cursor.isNull(_cursorIndexOfThumbnailUri)) {
              _tmpThumbnailUri = null;
            } else {
              _tmpThumbnailUri = _cursor.getString(_cursorIndexOfThumbnailUri);
            }
            _item = new ScanHistory(_tmpId,_tmpTitle,_tmpDateMillis,_tmpPdfUri,_tmpThumbnailUri);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public PagingSource<Integer, ScanHistory> getScansPagingSource() {
    final String _sql = "SELECT * FROM scan_history ORDER BY dateMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return new LimitOffsetPagingSource<ScanHistory>(_statement, __db, "scan_history") {
      @Override
      @NonNull
      protected List<ScanHistory> convertRows(@NonNull final Cursor cursor) {
        final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(cursor, "id");
        final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(cursor, "title");
        final int _cursorIndexOfDateMillis = CursorUtil.getColumnIndexOrThrow(cursor, "dateMillis");
        final int _cursorIndexOfPdfUri = CursorUtil.getColumnIndexOrThrow(cursor, "pdfUri");
        final int _cursorIndexOfThumbnailUri = CursorUtil.getColumnIndexOrThrow(cursor, "thumbnailUri");
        final List<ScanHistory> _result = new ArrayList<ScanHistory>(cursor.getCount());
        while (cursor.moveToNext()) {
          final ScanHistory _item;
          final long _tmpId;
          _tmpId = cursor.getLong(_cursorIndexOfId);
          final String _tmpTitle;
          _tmpTitle = cursor.getString(_cursorIndexOfTitle);
          final long _tmpDateMillis;
          _tmpDateMillis = cursor.getLong(_cursorIndexOfDateMillis);
          final String _tmpPdfUri;
          if (cursor.isNull(_cursorIndexOfPdfUri)) {
            _tmpPdfUri = null;
          } else {
            _tmpPdfUri = cursor.getString(_cursorIndexOfPdfUri);
          }
          final String _tmpThumbnailUri;
          if (cursor.isNull(_cursorIndexOfThumbnailUri)) {
            _tmpThumbnailUri = null;
          } else {
            _tmpThumbnailUri = cursor.getString(_cursorIndexOfThumbnailUri);
          }
          _item = new ScanHistory(_tmpId,_tmpTitle,_tmpDateMillis,_tmpPdfUri,_tmpThumbnailUri);
          _result.add(_item);
        }
        return _result;
      }
    };
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
