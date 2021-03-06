<template>
  <body>
    <div class="login-container">
      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        autocomplete="on"
        label-position="left"
      >
        <div class="title-container">
          <h3 class="title">
            Login
          </h3>
        </div>

        <el-form-item prop="username">
          <span class="svg-container">
            <svg-icon icon-class="user" />
          </span>
          <el-input
            ref="username"
            v-model="loginForm.username"
            placeholder="Username"
            name="username"
            type="text"
            tabindex="1"
            autocomplete="on"
          />
        </el-form-item>

        <el-tooltip
          v-model="capsTooltip"
          content="Caps lock is On"
          placement="right"
          manual
        >
          <el-form-item v-if="!googleSignedIn" prop="password">
            <span class="svg-container">
              <svg-icon icon-class="password" />
            </span>
            <el-input
              :key="passwordType"
              ref="password"
              v-model="loginForm.password"
              :type="passwordType"
              placeholder="Password"
              name="password"
              tabindex="2"
              autocomplete="on"
              @keyup.native="checkCapslock"
              @blur="capsTooltip = false"
              @keyup.enter.native="handleLogin"
            />
            <span
              class="show-pwd"
              @click="showPwd"
            >
              <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
            </span>
          </el-form-item>
        </el-tooltip>

        <el-link v-if="!googleSignedIn" :underline="false" @click="showDialog=true">
          Forgot password?
        </el-link>

        <el-button
          :loading="loading"
          type="primary"
          style="width:100%;margin-bottom:30px;"
          @click.native.prevent="handleLogin"
        >
          {{ this.loginText }}
        </el-button>

        <div id="my-signin2"></div>
        <div v-if="googleSignedIn">
          <el-button @click="googleSignout">Sign out from Google</el-button>
        </div>

        <div style="margin-top: -39px; float: right;">
          <el-button
            class="thirdparty-button"
            type="primary"
            @click="redirectHome"
          >
            Home
          </el-button>

          <el-button
            class="thirdparty-button"
            type="primary"
            @click="redirectRegister"
          >
            Register
          </el-button>

          <!-- <el-button style='float: right' class="thirdparty-button" type="primary" @click="showDialog=true">
            Or connect with
          </el-button> -->
        </div>
      </el-form>

      <el-dialog title="Password Reset" :visible.sync="showDialog">
        <p style="margin-bottom: 20px; margin-top: -10px;">
          Enter your email address and click Submit. You will get an email including a link to reset your password.
        </p>
        <el-form
          @submit.native.prevent="handlePasswordReset"
          ref="passwordResetForm"
          :rules="passwordResetRules"
          :model="passwordResetForm"
        >
          <el-form-item prop="email">
            <el-input ref="email" placeholder="Email" v-model="passwordResetForm.email" />
          </el-form-item>
          <el-button @click="handlePasswordReset">
            Submit
          </el-button>
        </el-form>
      </el-dialog>
    </div>
  </body>
</template>

<script>
import { validUsername, validEmail } from '@/utils/validate'
import { Message } from 'element-ui'

export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!validUsername(value)) {
        callback(new Error('Username can not be less than 3 characters'))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('Password can not be less than 6 characters'))
      } else {
        callback()
      }
    }
    const validateEmail = (rule, value, callback) => {
      if (!validEmail(value)) {
        callback(new Error('Please enter a valid email'))
      } else {
        callback()
      }
    }
    return {
      loginText: 'Login',
      appSecret: '878451092423-3ksgjtr0q19lrn9e6rdijdh0iddhl9pp.apps.googleusercontent.com',
      googleSignedIn: false,
      loginForm: {
        username: '',
        password: '',
      },
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      passwordResetForm: {
        email: ''
      },
      passwordResetRules: {
        email: [{ required: true, trigger: 'blur', validator: validateEmail }]
      },
      passwordType: 'password',
      capsTooltip: false,
      loading: false,
      showDialog: false,
      redirect: undefined,
      otherQuery: {}
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        const query = route.query
        if (query) {
          this.redirect = query.redirect
          this.otherQuery = this.getOtherQuery(query)
        }
      },
      immediate: true
    }
  },
  created() {
    // window.addEventListener('storage', this.afterQRScan)
  },
  mounted() {
    /* eslint-disable */
    var __this = this

    gapi.load('auth2', function(){
        gapi.auth2.init({
            client_id: __this.appSecret
        }).then(() => {
            gapi.signin2.render('my-signin2', {
                height: 39,
                theme: 'light',
                longtitle: false,
                onsuccess: __this.onSignIn
            })
        })
    })
    if (this.loginForm.username === '') {
      this.$refs.username.focus()
    } else if (this.loginForm.password === '') {
      this.$refs.password.focus()
    }
  },
  destroyed() {
    // window.removeEventListener('storage', this.afterQRScan)
  },
  methods: {
    checkCapslock({ shiftKey, key } = {}) {
      if (key && key.length === 1) {
        if (shiftKey && (key >= 'a' && key <= 'z') || !shiftKey && (key >= 'A' && key <= 'Z')) {
          this.capsTooltip = true
        } else {
          this.capsTooltip = false
        }
      }
      if (key === 'CapsLock' && this.capsTooltip === true) {
        this.capsTooltip = false
      }
    },
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    handleLogin() {
      // DEBUG
      // console.log(this.loginForm)

      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$store.dispatch('user/login', this.loginForm)
            .then(() => {
              this.$router.push({ path: this.redirect || '/profile', query: this.otherQuery })
              this.loading = false
            })
            .catch(() => {
              this.loading = false
            })
        } else {
          return false
        }
      })
    },
    getOtherQuery(query) {
      return Object.keys(query).reduce((acc, cur) => {
        if (cur !== 'redirect') {
          acc[cur] = query[cur]
        }
        return acc
      }, {})
    },
    redirectRegister() {
      this.$router.push({ path: '/register'})
    },
    redirectHome() {
      this.$router.push({ path: '/home'})
    },
    handlePasswordReset() {
      this.$refs.passwordResetForm.validate(valid => {
        if (valid) {
          this.$store.dispatch('user/resetPassword', this.passwordResetForm)
            .then(() => {
              Message.success('Password reset link sent to your email address')
              this.showDialog = false
            }).catch(() => {
          })
        } else {
          return false
        }
      })
    },
    onSignIn(googleUser) {
      var profile = googleUser.getBasicProfile()

      // DEBUG
      // console.log('Logged in.');
      // console.log('ID: ' + profile.getId()); // Do not send to your backend! Use an ID token instead.
      // console.log('Name: ' + profile.getName());
      // console.log('Email: ' + profile.getEmail()); // This is null if the 'email' scope is not present.

      delete this.loginForm.password
      this.loginForm.googleToken = profile.getId()

      document.getElementById("my-signin2").setAttribute('hidden', true)
      this.googleSignedIn = true
      this.loginText = "Login with Google"
    },

    googleSignout() {
        var auth2 = gapi.auth2.getAuthInstance()
        var __this = this

        auth2.signOut().then(function () {
            // DEBUG
            // console.log('User signed out.')

            delete __this.loginForm.googleToken
            __this.loginForm.password = ''

            __this.googleSignedIn = false
            __this.loginText = "Login"
            document.getElementById('my-signin2').removeAttribute('hidden')
        });
    }
  }
}

</script>

<style lang="scss">
/* 修复input 背景不协调 和光标变色 */
/* Detail see https://github.com/PanJiaChen/vue-element-admin/pull/927 */
$cursor: #424646;
$bg:#2d3a4b;
$dark_gray: #424646;
$light_gray:#eee;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .login-container .el-input input {
    color: $cursor;
  }
}

body {
  background: url("https://thewallpaper.co//wp-content/uploads/2016/03/black-and-white-city-houses-skyline-landscape-amazing-city-view-beautiful-place-wallpaper-free-city-photos-best-town-city-images-for-windows-large-places-background-1600x1024.jpg") no-repeat;
  -webkit-background-size: cover;
  -moz-background-size: cover;
  -o-background-size: cover;
  background-size: cover;
}

.login-container {
  .el-link {
    margin-top: -20px;
    margin-bottom: 20px;
    float: right;
  }

  .el-input {
    display: inline-block;
    height: 47px;
    width: 85%;

    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: $dark_gray;
      height: 47px;
      caret-color: $cursor;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px $light_gray inset !important;
        -webkit-text-fill-color: $cursor !important;
      }
    }
  }

  .el-button {
    transition-duration: 0.4s;
    text-decoration: none;
    font-size: 15px;
    cursor: pointer;
    color: $light_gray;
    border: 2px solid $dark_gray;
    border-radius: 4px;
    background-color: $dark_gray;
  }

  .el-button:hover {
    background-color: #f6f7f7;
    color: $dark_gray;
    border-color: #e7e7e7;
  }

  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
}
</style>

<style lang="scss" scoped>
$bg:#2d3a4b;
$dark_gray: #424646;
$light_gray:#eee;

.login-container {
  min-height: 100%;
  width: 100%;
  overflow: hidden;

  .login-form {
    position: relative;
    width: 520px;
    max-width: 100%;
    padding: 160px 35px 10px;
    margin: 0 auto;
    overflow: hidden;
  }

  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;

    span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
  }

  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }

  .title-container {
    position: relative;

    .title {
      font-size: 26px;
      color: $dark_gray;
      margin: 0px auto 40px auto;
      text-align: center;
      font-weight: bold;
    }
  }

  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }

  @media only screen and (max-width: 470px) {
    .thirdparty-button {
      display: none;
    }
  }
}
</style>
