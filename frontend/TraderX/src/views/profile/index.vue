<template>
  <div class="app-container">
    <div v-if="user">
      <el-row :gutter="20">
        <el-col
          :span="6"
          :xs="24"
        >
          <user-card :user="user" />
        </el-col>

        <el-col
          :span="18"
          :xs="24"
        >
          <el-card>
            <el-tabs v-model="activeTab">
              <el-tab-pane
                label="My Investments"
                name="myinvestments"
              >
                <my-investments/>
              </el-tab-pane>
              <el-tab-pane
                label="My Predictions"
                name="mypredictions"
              >
                <predictions/>
              </el-tab-pane>
              <el-tab-pane
                label="My Transactions"
                name="mytransactions"
              >
                <my-transactions/>
              </el-tab-pane>
              <el-tab-pane
                label="Portfolio"
                name="portfolio"
              >
                <portfolio :username="this.user.username"/>
              </el-tab-pane>
              <el-tab-pane
                label="My Articles"
                name="articles"
              >
                <articles />
              </el-tab-pane>
              <el-tab-pane
                label="Edit Profile"
                name="editprofile"
              >
                <editprofile :user="user" v-if="this.user.username"/>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import UserCard from './components/UserCard'
import Portfolio from './components/Portfolio'
import Articles from './components/Articles'
import Editprofile from './components/Editprofile'
import PrivateAccount from './components/PrivateAccount'
import Predictions from './components/Predictions'
import MyInvestments from '@/components/MyInvestments'
import MyTransactions from '@/components/MyTransactions'

export default {
  name: 'Profile',
  components: { UserCard, Portfolio, Articles, Editprofile, PrivateAccount, MyInvestments, MyTransactions, Predictions },
  data() {
    return {
      user: {},
      activeTab: 'myinvestments'
    }
  },
  computed: {
    ...mapGetters([
      'name',
      'avatar',
      'roles'
    ])
  },
  async created() {
    await this.getUserInfo()
  },
  methods: {
    async getUserInfo() {
      await this.$store.dispatch('user/getInfo').then(response =>{
        this.user = this.$store.getters.userInfo
      }).catch(error => {
        console.log(error)
      })
    },

  },
  mounted() {
  }
}
</script>

<style lang="scss">
  $cursor: #424646;
  $bg:#2d3a4b;
  $dark_gray: #424646;
  $light_gray:#eee;

  .app-main {
    background-color: $light_gray;
  }
</style>
