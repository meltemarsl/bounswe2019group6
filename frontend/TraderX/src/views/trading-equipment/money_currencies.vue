<template>
 
  <div>
    <el-row style="padding:16px 16px 0;margin-bottom:32px;">
      <el-card>
        <el-tabs v-model="activeTab">
          <el-tab-pane class='te-tab-pane' v-for="ed in equipmentData" :key="ed.key" :label="ed.label" :name="ed.key" :type="ed.key">
              <div v-if="activeTab==ed.key">
                <p class="te-info-text">Last 100 days' American Dollar / {{ ed.label }} with openning values is as follows:</p>
                <div class='chart-wrapper'>
                  <line-chart :type="ed.key" :chart-data="ed.data"/>
                </div>

                <p class="te-info-text">Last 20 days' American Dollar / {{ ed.label }} with openning, closing, highest and lowest values is as follows:</p>
                <div class='chart-wrapper'>
                  <line-chart-detailed :type="ed.key" :chart-data="ed.data"/>
                </div>

                <el-button class='change-base-button' @click="changeBaseofEquipment(ed.label)"><svg-icon style="margin-right:10px" icon-class="chart" />Change the Base</el-button>
                <el-button class='buy-button' @click="showBuyEquipmentDialog=true"><svg-icon style="margin-right:10px" icon-class="shopping" />Buy Equipment</el-button>
                
                <el-card class='container-in-tab'>
                  <p> alerts comes here </p>
                </el-card>

                <el-card class='container-in-tab'>
                  <h4> Most reviewed articles about {{ed.label}} </h4>

                  <!-- Got this from complex-table component -->
                  <el-table
                    :data="articleList"
                    border
                    fit
                    highlight-current-row
                    style="width: 100%;"
                  >
                    <el-table-column label="ID" prop="id" align="center">
                      <template slot-scope="scope">
                        <span>{{ scope.row.id }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="Date" align="center">
                      <template slot-scope="scope">
                        <span>{{ scope.row.timestamp | parseTime('{y}-{m}-{d} {h}:{i}') }}</span>
                      </template>
                    </el-table-column>
                    <!-- TODO: when pressed to one of the titles it should direct the user to the corresponding article -->
                    <el-table-column label="Title" >
                      <template slot-scope="{row}">
                        <span class="link-type">{{ row.title }}</span>
                        <!-- <el-tag>{{ row.type | typeFilter }}</el-tag> -->
                      </template>
                    </el-table-column>
                    <el-table-column label="Author" align="center">
                      <template slot-scope="scope">
                        <span>{{ scope.row.author }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="Importance" >
                      <template slot-scope="scope">
                        <svg-icon v-for="n in +scope.row.importance" :key="n" icon-class="star" class="meta-item__icon" />
                      </template>
                    </el-table-column>
                    <el-table-column label="Readings" align="center">
                      <template slot-scope="{row}">
                        <span v-if="row.pageviews" class="link-type">{{ row.pageviews }}</span>
                        <span v-else>0</span>
                      </template>
                    </el-table-column>
                  </el-table>
                  
                  <el-button class='read-article' style="margin-top:20px;" @click="readArticle(ed.label)"><svg-icon style="margin-right:10px" icon-class="shopping" /> Read More Articles about {{ ed.label }} </el-button>
                </el-card>

                <el-card class='container-in-tab'>
                  <h4> Comments about {{ ed.label }} </h4>
                  <el-button class='create-comment' style="margin-top:20px;margin-bottom:20px;" @click="showCreateCommentDialog=true">
                    <svg-icon style="margin-right:10px" icon-class="edit" /> Write Comment </el-button>
                  
                  <el-row class='row' v-for="c in ed.comments" :key="c.id" :gutter="20" style="padding:16px 16px 0;margin-bottom:20px;">
                    <el-card class='comment-container'>
                      <p>
                        <span class="comment-author"> {{ c.author }} </span>
                        <span class="comment-time"> {{ c.lastModifiedTime | parseTime('{y}-{m}-{d} {h}:{i}') }} - </span>
                        <span class="comment-options"> {{ c.likes }} Likes - </span>
                        <span class="comment-options"> {{ c.dislikes }} Dislikes </span>
                      </p>
                      <p class="comment-text"> {{ c.comment }} </p>
                      <p class="comment-options">
                        <a :class="{liked: c.status === 'LIKED'}" @click="likeComment(ed.key, c.id)"><i class="el-icon-arrow-up"/> Like </a> 
                        <a :class="{disliked: c.status === 'DISLIKED'}" @click="dislikeComment(ed.key, c.id)"><i class="el-icon-arrow-down"/> Dislike </a> |
                        <a @click="revokeComment(ed.key, c.id)"> Revoke Vote </a> |
                        <a class="delete-text" @click="deleteComment(ed.key, c.id)"><i class="el-icon-delete"/> Delete Comment </a> |
                        <a class="delete-text" @click="showEditCommentDialog=true;editCommentContent=c.comment"><i class="el-icon-edit"/> Edit Comment </a>

                        <el-dialog title="Edit Comment" :visible.sync="showEditCommentDialog">
                          <textarea class="comment-textarea" column="100" rows="10" v-model="editCommentContent"></textarea>
                          <div>
                            <el-button style="margin-top:10px;" @click="editComment(ed.key, c.id)"><svg-icon icon-class="edit"/> Edit Comment </el-button>
                          </div>
                        </el-dialog>

                      </p>
                    </el-card>
                  </el-row>

                  <el-dialog title="Create Comment" :visible.sync="showCreateCommentDialog">
                    <textarea class="comment-textarea" placeholder="Write your comment here" column="100" rows="10" v-model="createCommentContent"></textarea>
                    <div>
                      <el-button style="margin-top:10px;" @click="postComment(ed.key)"><svg-icon icon-class="edit"/> Publish Comment </el-button>
                    </div>
                  </el-dialog>

                  
                
                </el-card>

              </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </el-row>

    <el-dialog title="Select Trading Equipment" :visible.sync="showBuyEquipmentDialog">
      <el-input placeholder="Please enter an amount" v-model="buyamountinput" class="input-with-select">
        <el-select style="width:120px" v-model="select" slot="prepend" placeholder="Select">
          <div>
            <el-option v-for="(item, index) in this.equipmentData" :key="index" :label="item.key" :value="item.key"></el-option>
          </div>
        </el-select>
        <el-button slot="append" @click="buyEquipment()">Buy</el-button>
      </el-input>  
    </el-dialog>



  </div>
   
</template>

<script>
import LineChart from './components/LineChart'
import LineChartDetailed from './components/LineChartDetailed'

import { buyEquipment } from '@/api/equipment'

// The usual sorting in javascript sorts alphabetically which causes mistake in our code
const numberSort = function (a,b) {
    return a - b;
};

export default {
  name: 'DashboardAdmin',
  components: {
    LineChartDetailed,
    LineChart,
  },
  data() {
    return {
      chartData: {},
      showBuyEquipmentDialog: false,
      showCreateCommentDialog: false,
      showEditCommentDialog: false,
      showDialog: false,
      buyamountinput: '',
      select: '',
      createCommentContent: '',
      editCommentContent: '',
      // equipmentData is expected to be: 
      // [
      //   {
      //     label: Euro,
      //     key: EUR,
      //     data: {
      //       open: [...],
      //       close: [...],
      //       high: [...],
      //       low: [...],
      //       current: [...] 
      //     }
      //   }, 
      //   {
      //     label: Japanese Yen,
      //     key: JPY
      //   }, ...
      // ]
      equipmentData: [],
      activeTab: 'JPY',
      articleList: null,
      commentList: {},
      postCommentDict: {
        "comment": "",
      },
      editCommentDict: {
        "comment": "",
      },
    }
  },
  async created() {
    this.returnArticleList()
    
    var equipmentList = await this.getEquipmentList()
    this.getCommentList(equipmentList)
    // Fill the equipmentData with equipment list
    equipmentList.forEach(function(equipmentKey) {
      this.equipmentData.push({key: equipmentKey.code})
    }, this)
    var equipmentValues = await this.getEquipmentValues(equipmentList) 
    this.equipmentData = equipmentValues
    this.activeTab = this.equipmentData[0].key
  },
  methods: {

    // For now it returns mock data
    // TODO: change this method to get actual articles
    returnArticleList() {
      this.articleList = [
        {
          id: 1,
          timestamp: 1024316325463,
          title: 'Trolollolo',
          author: 'Irmos',
          importance: 3,
          pageviews: 245
        },

        {
          id: 2,
          timestamp: 1012345625463,
          title: 'Random articlezz',
          author: 'Sado',
          importance: 2,
          pageviews: 12420425
        },

        {
          id: 3,
          timestamp: 1024316325463,
          title: 'adfniun biyg oiniug',
          author: 'Yukseljim',
          importance: 3,
          pageviews: 15
        },

        {
          id: 4,
          timestamp: 1024313478569,
          title: 'U picku materinu',
          author: 'More Irmos',
          importance: 1,
          pageviews: 945231
        },

        {
          id: 5,
          timestamp: 1024313456789,
          title: 'My hands are typing words',
          author: 'Irmos',
          importance: 2,
          pageviews: 8245
        }
      ]
    },

    // Comment List will be received for all of the equipment
    async getCommentList(equipmentList) {
      var commentList = {}
      equipmentList.forEach(async function(e) {
        try {
          await this.$store.dispatch('comment/getCommentList', e.code.toLowerCase())
          var res = this.$store.getters.commentQueryResult
          commentList[e.code] = res
        } catch (error) {
          console.log(error)
        }
      }, this)
      this.commentList = commentList
    },

    // Promise for getting equipments list 
    async getEquipmentList(equipmentList) {
      try {
        await this.$store.dispatch('equipment/getAllCurrencies')
        var res = this.$store.getters.currencyResult
        return res.equipments
      } catch (error) {
        console.log(error)
        return error
      }  
    },

    // equipment = ['JPY', 'TRY', ...]
    async getEquipmentValues(equipmentList) {
      // equipmentValues = [{label="Turkish Lira", key="TRY", data={open=[5.6, 4.34, 7.54,...], close=[..], high=[..], low=[..]}}, {label="Japanese Yen", key="JPY", data={actualData=[12.2312, 11.234545, 10.23234, ...]}} ...]
      var equipmentValues = []
      
      equipmentList.forEach(async function(e) {
        try {
          await this.$store.dispatch('equipment/getEquipment', e.code.toLowerCase())
          var res = this.$store.getters.equipmentQueryResult
          equipmentValues.push({})
          equipmentValues[equipmentValues.length-1].key = res.equipment.code
          equipmentValues[equipmentValues.length-1].label = res.equipment.name
          equipmentValues[equipmentValues.length-1].currentValue = res.equipment.currentValue
          equipmentValues[equipmentValues.length-1].data = {
            open: [],
            close: [],
            high: [],
            low: [],
            current: [],
          }
          equipmentValues[equipmentValues.length-1].comments = this.commentList[res.equipment.code]
          res.historicalValues.forEach(function(val) {
            equipmentValues[equipmentValues.length-1].data.open.push(val.open)
            equipmentValues[equipmentValues.length-1].data.close.push(val.close)
            equipmentValues[equipmentValues.length-1].data.high.push(val.high)
            equipmentValues[equipmentValues.length-1].data.low.push(val.low)
            equipmentValues[equipmentValues.length-1].data.current.push(res.equipment.currentValue)
          })
        } catch (error) {
          console.log(error)
        }
      }, this)
      return equipmentValues
    },

    buyEquipment() {
      this.$store.dispatch('equipment/buyEquipment', {'code' : this.select, "amount" : this.buyamountinput}).then(response => {
        this.showBuyEquipmentDialog = false
        this.buyamountinput =  ''
        this.$message.success('Equipment Is Bought Successfully!')
      }).catch(err => {
        console.log(err)
      })
    },

    changeBaseofEquipment(equipmentLabel) {
      this.equipmentData.forEach(function(currency) {
        if (currency.label == equipmentLabel) {
          for (let i = 0; i < currency.data.open.length; i++) {
            currency.data.open[i] = 1/currency.data.open[i]
            currency.data.close[i] = 1/currency.data.close[i]
            currency.data.low[i] = 1/currency.data.low[i]
            currency.data.high[i] = 1/currency.data.high[i]
            currency.data.current[i] = 1/currency.data.current[i]
          }
        }
      }, this)
    },

    readArticle(equipmentLabel) {
      this.$notify({
        title: 'Success',
        message: 'Directing you to the articles about ' + equipmentLabel,
        type: 'success',
        duration: 2000
      })
    },

    postComment(equipmentCode) {
      this.postCommentDict["comment"] =  this.createCommentContent
      // Posting to backend
      this.$store.dispatch('comment/postComment', {"code": equipmentCode, "commentDict": this.postCommentDict}).then(() => {
        this.showCreateCommentDialog = false
        this.createCommentContent = ''
        this.$message.success('Comment is posted successfully!')
        
        var res = this.$store.getters.commentQueryResult
        this.equipmentData.forEach(function(e) {
          if (e.key == equipmentCode) {
            e.comments.push(res)
          }
        }, this)

      }).catch(err => {
        console.log(err)
      })
    },

    editComment(equipmentCode, commentId) {
      // Posting to backend
      this.editCommentDict["comment"] = this.editCommentContent
      this.$store.dispatch('comment/editComment', {"commentId": commentId, "commentDict": this.editCommentDict}).then(() => {
        
        var that = this
        this.equipmentData.forEach(function(e) {
          if (e.key == equipmentCode) {
            e.comments.forEach(function(c) {
              if (c.id == commentId) {
                c.comment = that.editCommentContent
              }
            })
          }
        })

        this.showEditCommentDialog = false
        this.editCommentContent = ''
        this.$message.success('Comment is edited successfully!')
      }).catch(err => {
        console.log(err)
      })
    },

    async deleteComment(equipmentCode, commentId) {
      var that = this
      this.$store.dispatch('comment/deleteComment', commentId).then(async function() {
        that.$message.success('Comment deleted!')
        that.equipmentData.forEach(function(e) {
          if (e.key == equipmentCode) {
            e.comments.forEach(function(comment) {
              if (comment.id == commentId) {
                comment.comment = "[This comment is deleted by the user...]"
              }
            })
          }
        }, this)
      }).catch(err => {
        console.log(err)
      })
        
    }, 

    likeComment(equipmentCode, commentId) {
      this.$store.dispatch('comment/voteComment', {"commentId": commentId, "voteType": "up"}).then(response => {
        this.$message.success('Comment liked!')
        this.equipmentData.forEach(function(e) {
          if (e.key == equipmentCode) {
            e.comments.forEach(function(comment) {
              if (comment.id == commentId) {
                comment.likes += 1
                comment.status = "LIKED"
              }
            })
          }
        }, this)
      }).catch(err => {
        console.log(err)
      })
    },

    dislikeComment(equipmentCode, commentId) {
      // Post to backend
      this.$store.dispatch('comment/voteComment', {"commentId": commentId, "voteType": "down"}).then(response => {
        this.$message.success('Comment disliked!')
        this.equipmentData.forEach(function(e) {
          if (e.key == equipmentCode) {
            e.comments.forEach(function(comment) {
              if (comment.id == commentId) {
                comment.dislikes += 1
                comment.status = "DISLIKED"
              }
            })
          }
        }, this)
      }).catch(err => {
        console.log(err)
      })
    },

    revokeComment(equipmentCode, commentId) {
      var lastStatus = ""
      this.equipmentData.forEach(function(e) {
        if (e.key == equipmentCode) {
          e.comments.forEach(function(comment) {
            if (comment.id == commentId) {
              lastStatus = comment.status
            }
          })
        }
      }, this)
      console.log('lastStatus is revokeComment : ' + lastStatus)
      this.$store.dispatch('comment/revokeVote', commentId).then(response => {
        this.$message.success('Last vote revoked!')
        this.equipmentData.forEach(function(e) {
          if (e.key == equipmentCode) {
            e.comments.forEach(function(comment) {
              if (comment.id == commentId) {
                if (lastStatus == "LIKED") {
                  comment.likes -= 1
                } else if (lastStatus == "DISLIKED") {
                  comment.dislikes -= 1
                } 
                comment.status = "NOT_COMMENTED"
              }
            })
          }
        }, this)
      }).catch(err => {
        console.log(err)
      })
    },
  }
}
</script>

<style lang="scss" scoped>

.container-in-tab {
  margin-top: 30px;
}

.comment-time {
  font-weight: normal;
  font-size: 8pt;
  color:#696969;
}

.comment-author {
  font-weight: bold;
  font-size: 11pt;
}

.comment-text {
  font-weight: normal;
  font-size: 10pt;
  color: #404040;
  white-space: pre-line;
}

.comment-options {
  font-weight: normal;
  font-size: 9pt;
  color: #696969
}

.disliked{
  font-weight: normal;
  font-size: 9pt;
  color: red;
}

.liked {
  font-weight: normal;
  font-size: 9pt;
  color: green;
}


.comment-textarea {
  resize: none;
  -webkit-box-sizing: border-box;
  -moz-box-sizing: border-box;
  box-sizing: border-box;
  width: 100%;
}

</style>
