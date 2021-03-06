package com.wopin.qingpaopao.bean.response;

import java.io.Serializable;
import java.util.List;

public class DrinkListTotalRsp extends NormalRsp implements Serializable {

    /**
     * status : 0
     * msg :
     * result : [{"_id":"5b96865b1c6a360f2907a99c","userId":"5b88fad74becec728bafe698","date":"20180910","target":8,"__v":1,"drinks":[{"time":"2018-09-10 22:57","_id":"5b96865b1c6a360f2907a99d"},{"time":"2018-09-10 22:57","_id":"5b9686631c6a360f2907a99e"}]}]
     */

    private List<ResultBean> result;

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean implements Serializable {
        /**
         * _id : 5b96865b1c6a360f2907a99c
         * userId : 5b88fad74becec728bafe698
         * date : 20180910
         * target : 8
         * __v : 1
         * drinks : [{"time":"2018-09-10 22:57","_id":"5b96865b1c6a360f2907a99d"},{"time":"2018-09-10 22:57","_id":"5b9686631c6a360f2907a99e"}]
         */

        private String _id;
        private String userId;
        private String date;
        private int target;
        private int __v;
        private List<DrinksBean> drinks;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getTarget() {
            return target;
        }

        public void setTarget(int target) {
            this.target = target;
        }

        public int get__v() {
            return __v;
        }

        public void set__v(int __v) {
            this.__v = __v;
        }

        public List<DrinksBean> getDrinks() {
            return drinks;
        }

        public void setDrinks(List<DrinksBean> drinks) {
            this.drinks = drinks;
        }

        public static class DrinksBean {
            /**
             * time : 2018-09-10 22:57
             * _id : 5b96865b1c6a360f2907a99d
             */

            private String time;
            private String _id;

            public String getTime() {
                return time;
            }

            public void setTime(String time) {
                this.time = time;
            }

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }
        }
    }
}
